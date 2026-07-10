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
        c.version,
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

    override fun findCouriersWithAnyFreeStorageForUpdate(): List<Courier> =
        jdbcClient.sql(
            """
            $COURIERS_WITH_STORAGE_PLACES
            WHERE EXISTS (
                SELECT 1
                FROM storage_places free_sp
                WHERE free_sp.courier_id = c.id
                    AND free_sp.order_id IS NULL
            )
            FOR UPDATE -- Ждем, пока освободится лучший курьер, а не берем свободного (худшего)
            """.trimIndent()
        )
            .query(ResultSetExtractor(::mapCouriers))

    override fun getAllCouriersForUpdate(): List<Courier> =
        jdbcClient.sql(
            """
            $COURIERS_WITH_STORAGE_PLACES
            ORDER BY c.id
            FOR UPDATE
            """.trimIndent()
        )
            .query(ResultSetExtractor(::mapCouriers))

    internal fun save(courier: Courier) {
        if (courier.version == 0L) {
            insert(courier)
            // После INSERT в базу улетела version = 1.
            // Значит, объект в памяти тоже должен стать version = 1.
            courier.incrementVersion()
        } else {
            update(courier)
            // После UPDATE в базе версия стала version + 1.
            // Синхронизируем объект в памяти.
            courier.incrementVersion()
        }
    }

    private fun insert(courier: Courier) {
        // Так как это INSERT, мы точно знаем, что начальная версия должна стать 1.
        // Мы можем передать это значение явно, основываясь на courier.version (0 + 1)
        val initialVersion = courier.version + 1

        jdbcClient.sql(
            """
            INSERT INTO couriers (id, version, name, speed, location_x, location_y)
            VALUES (:id, :version, :name, :speed, :locationX, :locationY)
            """.trimIndent()
        )
            .param("id", courier.id)
            .param("version", initialVersion) // Передаем честную 1, вычисленную в Kotlin коде
            .param("name", courier.name)
            .param("speed", courier.speed)
            .param("locationX", courier.location.x)
            .param("locationY", courier.location.y)
            .update()

        // 2. Вставляем слоты хранения курьера
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

    private fun update(courier: Courier) {
        // 1. Обновляем корень агрегата с проверкой оптимистичной блокировки
        val updated = jdbcClient.sql(
            """
            -- неявно блокирует существующую строку на время записи + оптимистично проверяет, не устарели ли наши данные в памяти по мы пытались обновиться, 
            -- предотвращение состояния гонки по принципу «Check-Then-Act».
            UPDATE couriers -- в самом SQL-запросе UPDATE пессимистическая блокировка происходит автоматически на уровне движка базы данных 
            SET
                version = version + 1,
                name = :name,
                speed = :speed,
                location_x = :locationX,
                location_y = :locationY
            WHERE id = :id
                AND version = :version
            """.trimIndent()
        )
            .param("id", courier.id)
            .param("version", courier.version)
            .param("name", courier.name)
            .param("speed", courier.speed)
            .param("locationX", courier.location.x)
            .param("locationY", courier.location.y)
            .update()

        check(updated == 1) {
            "Optimistic lock failed for courier ${courier.id}"
        }

        // 2. Обновляем дочерние сущности
        courier.storagePlaces.forEach { storagePlace ->
            val updatedStoragePlace = jdbcClient.sql(
                """
            UPDATE storage_places
            SET
                name = :name,
                total_volume = :totalVolume,
                order_id = :orderId
            WHERE id = :id
                AND courier_id = :courierId
            """.trimIndent()
            )
                .param("id", storagePlace.id)
                .param("name", storagePlace.name.name)
                .param("totalVolume", storagePlace.totalVolume)
                .param("orderId", storagePlace.orderId)
                .param("courierId", courier.id)
                .update()

            check(updatedStoragePlace == 1) {
                "Storage place ${storagePlace.id} not updated"
            }
        }
    }

    private fun mapCouriers(rs: ResultSet): List<Courier> {
        val couriers = linkedMapOf<UUID, Courier>()

        while (rs.next()) {
            val courierId = UUID.fromString(rs.getString("id"))

            val courier = couriers.getOrPut(courierId) {
                Courier.restore(
                    id = courierId,
                    version = rs.getLong("version"),
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
