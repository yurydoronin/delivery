package delivery.core.application

import arrow.core.left
import arrow.core.right
import delivery.core.application.ports.input.commands.OrderAssignmentError
import delivery.core.application.ports.input.commands.OrderAssignmentUseCaseImpl
import delivery.core.application.ports.output.CourierRepositoryPort
import delivery.core.application.ports.output.OrderRepositoryPort
import delivery.core.application.ports.output.UnitOfWork
import delivery.core.domain.kernel.Location
import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.order.Order
import delivery.core.domain.services.DispatchError
import delivery.core.domain.services.OrderDispatcher
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import org.junit.jupiter.api.Test

class OrderAssignmentServiceTest {

    val courierRepository: CourierRepositoryPort = mockk(relaxed = true)
    val orderRepository: OrderRepositoryPort = mockk(relaxed = true)
    val orderDispatcher: OrderDispatcher = mockk(relaxed = true)
    val unitOfWork: UnitOfWork = mockk(relaxed = true)

    val sut = OrderAssignmentUseCaseImpl(
        courierRepository,
        orderRepository,
        orderDispatcher,
        unitOfWork
    )

    @Test
    fun `assigns order`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.of(3, 3), 1)
        val courier1 = Courier.of("Маша", 3, Location.of(4, 4))
        val courier2 = Courier.of("Коля", 1, Location.of(2, 2))
        val couriers = listOf(courier1, courier2)

        every { orderRepository.findAnyCreated() } returns order
        every { courierRepository.getAvailableCouriers() } returns couriers
        every { orderDispatcher.dispatch(order, couriers) } returns courier1.right()

        // Act
        val result = sut.execute()

        // Assert
        result.shouldBeRight()
        verify { unitOfWork.commit() }
    }

    @Test
    fun `fails to assign when no orders`() {
        // Arrange
        every { orderRepository.findAnyCreated() } returns null

        // Act
        val result = sut.execute()

        // Assert
        result shouldBe OrderAssignmentError.OrderNotFound.left()
    }

    @Test
    fun `fails to assign when no courier available`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.of(3, 3), 1)
        val couriers = emptyList<Courier>()

        every { orderRepository.findAnyCreated() } returns order
        every { courierRepository.getAvailableCouriers() } returns couriers
        every { orderDispatcher.dispatch(order, couriers) } returns DispatchError.NoAvailableCourier.left()

        // Act
        val result = sut.execute()

        // Assert
        result shouldBe DispatchError.NoAvailableCourier.left()
    }
}
