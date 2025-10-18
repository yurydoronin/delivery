package delivery.core.application

import arrow.core.left
import delivery.core.application.ports.input.queries.ActiveOrdersError
import delivery.core.application.ports.input.queries.GetActiveOrdersUseCaseImpl
import delivery.core.domain.kernel.Location
import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.order.Order
import delivery.infrastructure.output.adapters.postgres.BaseRepositoryTest
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GetActiveOrdersServiceTest @Autowired constructor(
    private val em: EntityManager,
    private val sut: GetActiveOrdersUseCaseImpl
) : BaseRepositoryTest() {

    @Test
    fun `get active orders`() {
        // Arrange
        val courier = Courier.of("Маша", 4, Location.of(1, 1))
        em.persist(courier)

        val order1 = Order.of(UUID.randomUUID(), Location.of(3, 3), 4)
        val order2 = Order.of(UUID.randomUUID(), Location.of(4, 4), 7)
        order1.assignToCourier(courier.id)
        em.persist(order1)
        em.persist(order2)
        em.flush() // чтобы JdbcTemplate увидел данные

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
