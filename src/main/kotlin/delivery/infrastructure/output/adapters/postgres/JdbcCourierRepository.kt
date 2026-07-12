package delivery.infrastructure.output.adapters.postgres

import delivery.core.application.ports.output.AggregateTracker
import delivery.core.application.ports.output.CourierRepositoryPort
import delivery.core.domain.kernel.Location
import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.courier.StoragePlace
import delivery.core.domain.model.courier.StoragePlaceName
import java.sql.ResultSet
import java.util.UUID
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

private const val COURIERS_WITH_STORAGE_PLACES = """
    SELECT
        c.id,
        c.name,
        c.speed,
        c.location_x,
        c.location_y,
        sp.id   AS sp_id,
        sp.name AS sp_name,
        sp.total_volume,
        sp.order_id
    FROM couriers c
        JOIN storage_places sp 
            ON sp.courier_id = c.id
    """

@Repository
class JdbcCourierRepository(
    private val aggregateTracker: AggregateTracker,
    private val jdbcClient: JdbcClient,
) : CourierRepositoryPort {

    override fun track(courier: Courier) {
        aggregateTracker.track(courier)
    }

    override fun get(courierId: UUID): Courier? =
        jdbcClient.sql(
            """
            $COURIERS_WITH_STORAGE_PLACES
            WHERE c.id = :id
            """.trimIndent()
        )
            .param("id", courierId)
            .query(ResultSetExtractor(::mapCouriers))
            .firstOrNull()

    override fun findCouriersWithAnyFreeStorage(): List<Courier> =
        jdbcClient.sql(
            """
            $COURIERS_WITH_STORAGE_PLACES
            WHERE EXISTS (
                SELECT 1
                FROM storage_places free_sp
                WHERE free_sp.courier_id = c.id
                    AND free_sp.order_id IS NULL
            )
            """.trimIndent()
        )
            .query(ResultSetExtractor(::mapCouriers))

    override fun getCouriersWithAssignedOrders(): List<Courier> =
        jdbcClient.sql(
            """
            $COURIERS_WITH_STORAGE_PLACES
            WHERE EXISTS (
                SELECT 1
                FROM orders o
                WHERE o.courier_id = c.id
                    AND o.status = 'ASSIGNED'
            )
            """.trimIndent()
        )
            .query(ResultSetExtractor(::mapCouriers))

    internal fun save(courier: Courier) {
        // 1. Атомарный UPSERT курьера
        jdbcClient.sql(
            """
            INSERT INTO couriers (id, name, speed, location_x, location_y)
            VALUES (:id, :name, :speed, :locationX, :locationY)
            ON CONFLICT (id) 
            DO UPDATE SET 
                name = EXCLUDED.name,
                speed = EXCLUDED.speed,
                location_x = EXCLUDED.location_x,
                location_y = EXCLUDED.location_y
            """.trimIndent()
        )
            .param("id", courier.id)
            .param("name", courier.name)
            .param("speed", courier.speed)
            .param("locationX", courier.location.x)
            .param("locationY", courier.location.y)
            .update()

        // 2. Обновление мест хранения
        // Чтобы не возиться с UPSERT для каждого слота, проще перетереть их в рамках одной транзакции Unit of Work
        jdbcClient.sql("DELETE FROM storage_places WHERE courier_id = :courierId")
            .param("courierId", courier.id)
            .update()

        courier.storagePlaces.forEach { storagePlace ->
            jdbcClient.sql(
                """
                INSERT INTO storage_places (id, name, total_volume, order_id, courier_id)
                VALUES (:id, :name, :totalVolume, :orderId, :courierId)
                """.trimIndent()
            )
                .param("id", storagePlace.id)
                .param("name", storagePlace.name.name)
                .param("totalVolume", storagePlace.totalVolume)
                .param("orderId", storagePlace.orderId)
                .param("courierId", courier.id)
                .update()
        }
    }

    private fun mapCouriers(rs: ResultSet): List<Courier> {
        val couriers = linkedMapOf<UUID, Courier>()

        while (rs.next()) {
            val courierId = UUID.fromString(rs.getString("id"))

            val courier = couriers.getOrPut(courierId) {
                Courier.restore(
                    id = courierId,
                    name = rs.getString("name"),
                    speed = rs.getInt("speed"),
                    location = Location.restore(
                        rs.getInt("location_x"),
                        rs.getInt("location_y")
                    )
                )
            }

            courier.restoreStoragePlace(
                StoragePlace.restore(
                    id = UUID.fromString(rs.getString("sp_id")),
                    name = StoragePlaceName.restore(rs.getString("sp_name")),
                    totalVolume = rs.getInt("total_volume"),
                    orderId = rs.getString("order_id")?.let(UUID::fromString)
                )
            )
        }

        return couriers.values.toList()
    }
}
