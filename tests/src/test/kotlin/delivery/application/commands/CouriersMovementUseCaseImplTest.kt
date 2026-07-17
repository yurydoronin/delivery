package delivery.application.commands

import arrow.core.left
import arrow.core.raise.either
import delivery.application.ports.input.commands.CouriersMovementUseCaseImpl
import delivery.application.ports.input.commands.MovementError
import delivery.application.ports.output.CourierRepositoryPort
import delivery.application.ports.output.OrderRepositoryPort
import delivery.application.ports.output.UnitOfWork
import delivery.domain.kernel.Location
import delivery.domain.model.courier.Courier
import delivery.domain.model.order.Order
import delivery.domain.model.order.OrderStatus
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import org.junit.jupiter.api.Test

class CouriersMovementUseCaseImplTest {

    val courierRepository: CourierRepositoryPort = mockk(relaxed = true)
    val orderRepository: OrderRepositoryPort = mockk(relaxed = true)
    val unitOfWork: UnitOfWork = mockk(relaxed = true)

    val sut = CouriersMovementUseCaseImpl(
        courierRepository,
        orderRepository,
        unitOfWork
    )

    @Test
    fun `fails to move if no couriers`() {
        // Arrange
        every { courierRepository.getCouriersWithAssignedOrders() } returns emptyList()
        every { orderRepository.findAllAssigned() } returns listOf()

        // Act
        val result = sut.execute()

        // Assert
        result shouldBe MovementError.NoCouriers.left()
    }

    @Test
    fun `fails to move if no assigned orders`() {
        either {
            // Arrange
            val courier = Courier.of("Маша", 1, Location.of(1, 1)).bind()
            every { courierRepository.getCouriersWithAssignedOrders() } returns listOf(courier)
            every { orderRepository.findAllAssigned() } returns emptyList()

            // Act
            val result = sut.execute().bind()

            // Assert
            result shouldBe MovementError.NoOrders.left()
        }
    }

    @Test
    fun `move couriers`() {
        either {
            // Arrange
            val courier1 = Courier.of("Маша", 4, Location.of(1, 1)).bind()
            val order1 = Order.of(UUID.randomUUID(), Location.of(3, 3), 1)
            order1.assignToCourier(courier1.id)

            every { courierRepository.getCouriersWithAssignedOrders() } returns listOf(courier1)
            every { orderRepository.findAllAssigned() } returns listOf(order1)

            // Act
            val result = sut.execute()

            // Assert
            result.shouldBeRight()
            courier1.location shouldBe order1.location
            order1.status shouldBe OrderStatus.COMPLETED
            verify { unitOfWork.commit() }
        }
    }
}