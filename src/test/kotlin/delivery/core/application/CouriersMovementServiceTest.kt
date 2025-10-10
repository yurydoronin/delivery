package delivery.core.application

import arrow.core.left
import delivery.core.application.ports.output.CourierRepositoryPort
import delivery.core.application.ports.output.OrderRepositoryPort
import delivery.core.application.ports.output.UnitOfWork
import delivery.core.domain.kernel.Location
import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.order.Order
import delivery.core.domain.model.order.OrderStatus
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import org.junit.jupiter.api.Test

class CouriersMovementServiceTest {

    val courierRepository: CourierRepositoryPort = mockk(relaxed = true)
    val orderRepository: OrderRepositoryPort = mockk(relaxed = true)
    val unitOfWork: UnitOfWork = mockk(relaxed = true)

    val sut = CouriersMovementService(
        courierRepository,
        orderRepository,
        unitOfWork
    )

    @Test
    fun `fails to move if no couriers`() {
        // Arrange
        every { courierRepository.getAllCouriers() } returns emptyList()
        every { orderRepository.findAllAssigned() } returns listOf()

        // Act
        val result = sut.move()

        // Assert
        result shouldBe MovementError.NoCouriers.left()
    }

    @Test
    fun `fails to move if no assigned orders`() {
        // Arrange
        val courier = Courier.of("Маша", 1, Location.of(1, 1))

        every { courierRepository.getAllCouriers() } returns listOf(courier)
        every { orderRepository.findAllAssigned() } returns emptyList()

        // Act
        val result = sut.move()

        // Assert
        result shouldBe MovementError.NoOrders.left()
    }

    @Test
    fun `move couriers`() {
        // Arrange
        val courier1 = Courier.of("Маша", 4, Location.of(1, 1))
        val courier2 = Courier.of("Коля", 1, Location.of(2, 2))
        val order1 = Order.of(UUID.randomUUID(), Location.of(3, 3), 1)
        order1.assignToCourier(courier1.id)

        every { courierRepository.getAllCouriers() } returns listOf(courier1, courier2)
        every { orderRepository.findAllAssigned() } returns listOf(order1)

        // Act
        val result = sut.move()

        // Assert
        result.shouldBeRight()
        // courier1 достиг заказа
        courier1.location shouldBe order1.location
        order1.status shouldBe OrderStatus.COMPLETED
        // courier2 остался на месте, так как заказа для него нет
        courier2.location shouldBe Location.of(2, 2)
        verify { unitOfWork.commit() }
    }
}