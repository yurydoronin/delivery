package delivery.infrastructure.output.adapters.postgres

import delivery.core.application.ports.output.AggregateTracker
import delivery.core.application.ports.output.OrderRepositoryPort
import delivery.core.domain.kernel.Location
import delivery.core.domain.model.order.Order
import delivery.core.domain.model.order.OrderStatus
import java.sql.ResultSet
import java.util.UUID
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
class JdbcOrderRepository(
    private val aggregateTracker: AggregateTracker,
    private val jdbcClient: JdbcClient,
) : OrderRepositoryPort {

    override fun track(order: Order) {
        aggregateTracker.track(order)
    }

    override fun get(orderId: UUID): Order? =
        jdbcClient.sql(
            """
            SELECT *
            FROM orders
            WHERE id = :id
            """.trimIndent()
        )
            .param("id", orderId)
            .query(::toDomain)
            .optional()
            .orElse(null)

    override fun findAnyCreated(): Order? =
        jdbcClient.sql(
            """
            SELECT *
            FROM orders
            WHERE status = :status
            LIMIT 1
            """.trimIndent()
        )
            .param("status", OrderStatus.CREATED.name)
            .query(::toDomain)
            .optional()
            .orElse(null)

    override fun findAllAssigned(): List<Order> =
        jdbcClient.sql(
            """
            SELECT *
            FROM orders
            WHERE status = :status
            """.trimIndent()
        )
            .param("status", OrderStatus.ASSIGNED.name)
            .query(::toDomain)
            .list()

    internal fun save(order: Order) {
        if (order.version == 0L) {
            insert(order)
            order.incrementVersion()
        } else {
            update(order)
            order.incrementVersion()
        }
    }

    private fun insert(order: Order) {
        val initialVersion = order.version + 1

        jdbcClient.sql(
            """
        INSERT INTO orders (id, version, location_x, location_y, volume, status, courier_id)
        VALUES (:id, :version, :locationX, :locationY, :volume, :status, :courierId)
        """.trimIndent()
        )
            .param("id", order.id)
            .param("version", initialVersion)
            .param("locationX", order.location.x)
            .param("locationY", order.location.y)
            .param("volume", order.volume)
            .param("status", order.status.name)
            .param("courierId", order.courierId)
            .update()
    }

    private fun update(order: Order) {
        val updated = jdbcClient.sql(
            """
        UPDATE orders
        SET
            version = version + 1,
            location_x = :locationX,
            location_y = :locationY,
            volume = :volume,
            status = :status,
            courier_id = :courierId
        WHERE id = :id
            AND version = :version
        """.trimIndent()
        )
            .param("id", order.id)
            .param("version", order.version)
            .param("locationX", order.location.x)
            .param("locationY", order.location.y)
            .param("volume", order.volume)
            .param("status", order.status.name)
            .param("courierId", order.courierId)
            .update()

        check(updated == 1) {
            "Optimistic lock failed for order ${order.id}"
        }
    }


    private fun toDomain(rs: ResultSet, rowNum: Int): Order =
        Order.restore(
            id = UUID.fromString(rs.getString("id")),
            version = rs.getLong("version"),
            location = Location.restore(
                rs.getInt("location_x"),
                rs.getInt("location_y"),
            ),
            volume = rs.getInt("volume"),
            status = OrderStatus.valueOf(rs.getString("status")),
            courierId = rs.getString("courier_id")?.let(UUID::fromString),
        )
}
