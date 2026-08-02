package delivery.application.commands

import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import delivery.application.ports.input.commands.AssignOrderUseCaseImpl
import delivery.application.ports.input.commands.OrderAssignmentError
import delivery.application.ports.output.CourierRepositoryPort
import delivery.application.ports.output.OrderRepositoryPort
import delivery.application.ports.output.UnitOfWork
import delivery.domain.kernel.LocationTestData
import delivery.domain.model.courier.Courier
import delivery.domain.model.courier.CourierType
import delivery.domain.model.order.Order
import delivery.domain.services.DispatchError
import delivery.domain.services.OrderDispatcher
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import org.junit.jupiter.api.Test

class AssignOrderUseCaseImplTest {

    val courierRepository: CourierRepositoryPort = mockk(relaxed = true)
    val orderRepository: OrderRepositoryPort = mockk(relaxed = true)
    val orderDispatcher: OrderDispatcher = mockk(relaxed = true)
    val unitOfWork: UnitOfWork = mockk(relaxed = true)

    val sut = AssignOrderUseCaseImpl(
        courierRepository,
        orderRepository,
        orderDispatcher,
        unitOfWork
    )

    @Test
    fun `assigns order`() {
        either {
            // Arrange
            val order = Order.of(UUID.randomUUID(), LocationTestData.random(), 1).bind()
            val courier1 = Courier.of(CourierType.WALKING, 3, LocationTestData.random()).bind()
            val courier2 = Courier.of(CourierType.WALKING, 1, LocationTestData.random()).bind()
            val couriers = listOf(courier1, courier2)

            every { orderRepository.findById(order.id) } returns order
            every { courierRepository.findCouriersWithAnyFreeStorage() } returns couriers
            every { orderDispatcher.dispatch(order, couriers) } returns courier1.right()

            // Act
            val result = sut.execute(order.id)

            // Assert
            result.shouldBeRight()
            verify { unitOfWork.commit() }
        }
    }

    @Test
    fun `fails to assign when no orders`() {
        // Arrange
        val orderId = UUID.randomUUID()

        every { orderRepository.findById(orderId) } returns null

        // Act
        val result = sut.execute(orderId)

        // Assert
        result shouldBe OrderAssignmentError.OrderNotFound.left()
    }

    @Test
    fun `fails to assign when no courier available`() {
        either {
            // Arrange
            val order = Order.of(UUID.randomUUID(), LocationTestData.random(), 1).bind()
            val couriers = emptyList<Courier>()

            every { orderRepository.findById(order.id) } returns order
            every { courierRepository.findCouriersWithAnyFreeStorage() } returns couriers
            every { orderDispatcher.dispatch(order, couriers) } returns DispatchError.NoAvailableCourier.left()

            // Act
            val result = sut.execute(order.id)

            // Assert
            result shouldBe DispatchError.NoAvailableCourier.left()
        }
    }
}
