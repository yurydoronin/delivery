package delivery.core.application

import arrow.core.left
import delivery.core.application.ports.input.queries.ActiveOrdersError
import delivery.core.application.ports.input.queries.GetActiveOrdersUseCaseImpl
import delivery.core.domain.kernel.Location
import delivery.core.domain.model.order.Order
import delivery.infrastructure.output.adapters.postgres.BaseRepositoryTest
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

class GetActiveOrdersUseCaseImplTest @Autowired constructor(
    private val jdbcTemplate: JdbcTemplate,
    private val sut: GetActiveOrdersUseCaseImpl
) : BaseRepositoryTest() {

    @Test
    fun `get active orders`() {
        // Arrange
        val courierId = UUID.randomUUID()

        jdbcTemplate.update(
            """
                INSERT INTO couriers(
                    id,
                    version,
                    name,
                    speed,
                    location_x,
                    location_y
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent(),
            courierId, 0L, "Маша", 4, 1, 1
        )

        val order1 = Order.of(UUID.randomUUID(), Location.of(3, 3), 4)
        val order2 = Order.of(UUID.randomUUID(), Location.of(4, 4), 7)
        order1.assignToCourier(courierId)

        jdbcTemplate.update(
            """
            INSERT INTO orders(
                id,
                version,
                location_x,
                location_y,
                volume,
                status,
                courier_id
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            order1.id,
            order1.version,
            order1.location.x,
            order1.location.y,
            order1.volume,
            order1.status.name,
            order1.courierId
        )

        jdbcTemplate.update(
            """
            INSERT INTO orders(
                id,
                version,
                location_x,
                location_y,
                volume,
                status,
                courier_id
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """
                .trimIndent(),
            order2.id,
            order2.version,
            order2.location.x,
            order2.location.y,
            order2.volume,
            order2.status.name,
            order2.courierId
        )

        // Act
        val result = sut.execute()

        // Assert
        val orders = result.shouldBeRight()
        orders.size shouldBe 2
        orders.map { it.location } shouldBe listOf(order1.location, order2.location)
    }

    @Test
    fun `fails when no incomplete orders`() {
        val result = sut.execute()

        result shouldBe ActiveOrdersError.NoActiveOrders.left()
    }
}
