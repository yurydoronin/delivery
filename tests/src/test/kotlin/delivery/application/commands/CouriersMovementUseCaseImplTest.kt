package delivery.application.commands

import delivery.application.ports.input.commands.CouriersMovementUseCaseImpl
import delivery.application.ports.input.commands.MovementError
import delivery.application.ports.output.CourierRepositoryPort
import delivery.application.ports.output.OrderRepositoryPort
import delivery.application.ports.output.UnitOfWork
import delivery.domain.kernel.Location
import delivery.domain.kernel.LocationTestData
import delivery.domain.model.courier.Courier
import delivery.domain.model.order.Order
import delivery.domain.model.order.OrderStatus
import io.kotest.assertions.arrow.core.shouldBeLeft
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
        val result = sut.execute().shouldBeLeft()

        // Assert
        result shouldBe MovementError.NoCouriers
    }

    @Test
    fun `fails to move if no assigned orders`() {
        // Arrange
        val courier = Courier.of("Маша", 1, LocationTestData.random()).shouldBeRight()
        every { courierRepository.getCouriersWithAssignedOrders() } returns listOf(courier)
        every { orderRepository.findAllAssigned() } returns emptyList()

        // Act
        val result = sut.execute().shouldBeLeft()

        // Assert
        result shouldBe MovementError.NoOrders
    }

    @Test
    fun `move couriers`() {
        // Arrange
        val order1 = Order.of(UUID.randomUUID(), Location.restore(3, 3), 1).shouldBeRight()
        val courier1 = Courier.of("Маша", 4, Location.restore(1,1)).shouldBeRight()
        order1.assignToCourier(courier1.id)

        every { courierRepository.getCouriersWithAssignedOrders() } returns listOf(courier1)
        every { orderRepository.findAllAssigned() } returns listOf(order1)

        // Act
        sut.execute().shouldBeRight()

        // Assert
        courier1.location shouldBe order1.location
        order1.status shouldBe OrderStatus.COMPLETED
        verify { unitOfWork.commit() }
    }
}
