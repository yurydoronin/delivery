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
        jdbcClient.sql(
            """
            INSERT INTO orders (id, location_x, location_y, volume, status, courier_id)
            VALUES (:id, :locationX, :locationY, :volume, :status, :courierId)
            ON CONFLICT (id) 
            DO UPDATE SET 
                location_x = EXCLUDED.location_x,
                location_y = EXCLUDED.location_y,
                volume = EXCLUDED.volume,
                status = EXCLUDED.status,
                courier_id = EXCLUDED.courier_id
            """.trimIndent()
        )
            .param("id", order.id)
            .param("locationX", order.location.x)
            .param("locationY", order.location.y)
            .param("volume", order.volume)
            .param("status", order.status.name)
            .param("courierId", order.courierId)
            .update()
    }

    private fun toDomain(rs: ResultSet, rowNum: Int): Order =
        Order.restore(
            id = UUID.fromString(rs.getString("id")),
            location = Location.restore(
                rs.getInt("location_x"),
                rs.getInt("location_y"),
            ),
            volume = rs.getInt("volume"),
            status = OrderStatus.valueOf(rs.getString("status")),
            courierId = rs.getString("courier_id")?.let(UUID::fromString),
        )
}
