package delivery.application.queries

import delivery.BaseRepositoryTest
import delivery.application.ports.input.queries.ActiveOrdersError
import delivery.application.ports.input.queries.GetActiveOrdersUseCaseImpl
import delivery.domain.kernel.LocationTestData
import delivery.domain.model.order.Order
import io.kotest.assertions.arrow.core.shouldBeLeft
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
                    name,
                    speed,
                    location_x,
                    location_y
                )
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
            courierId, "Маша", 4, 1, 1
        )

        val order1 = Order.of(UUID.randomUUID(), LocationTestData.random(), 4).shouldBeRight()
        val order2 = Order.of(UUID.randomUUID(), LocationTestData.random(), 7).shouldBeRight()
        order1.assignToCourier(courierId)

        jdbcTemplate.update(
            """
            INSERT INTO orders(
                id,
                location_x,
                location_y,
                volume,
                status,
                courier_id
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            order1.id,
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
                location_x,
                location_y,
                volume,
                status,
                courier_id
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            order2.id,
            order2.location.x,
            order2.location.y,
            order2.volume,
            order2.status.name,
            order2.courierId
        )

        // Act
        val result = sut.execute().shouldBeRight()

        // Assert
        result.size shouldBe 2
    }

    @Test
    fun `fails when no incomplete orders`() {
        val result = sut.execute().shouldBeLeft()

        result shouldBe ActiveOrdersError.NoActiveOrders
    }
}
