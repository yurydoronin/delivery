package delivery.application.commands

import arrow.core.Either
import delivery.application.ports.input.commands.CouriersMovementUseCaseImpl
import delivery.application.ports.input.commands.MovementError
import delivery.application.ports.output.CourierRepositoryPort
import delivery.application.ports.output.OrderRepositoryPort
import delivery.application.ports.output.UnitOfWork
import delivery.application.ports.output.atomic.AtomicOperationPort
import delivery.common.types.error.BusinessError
import delivery.domain.kernel.Location
import delivery.domain.kernel.LocationTestData
import delivery.domain.model.courier.Courier
import delivery.domain.model.courier.CourierType
import delivery.domain.model.order.Order
import delivery.domain.model.order.OrderStatus
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CouriersMovementUseCaseImplTest {

    val atomicOperation: AtomicOperationPort = mockk(relaxUnitFun = true)
    val courierRepository: CourierRepositoryPort = mockk(relaxed = true)
    val orderRepository: OrderRepositoryPort = mockk(relaxed = true)
    val unitOfWork: UnitOfWork = mockk(relaxed = true)

    val sut = CouriersMovementUseCaseImpl(
        atomicOperation,
        courierRepository,
        orderRepository,
        unitOfWork
    )

    @BeforeEach
    fun setUp(){
        every {
            atomicOperation.execute<BusinessError, Unit>(any(), any())
        } answers {
            secondArg<() -> Either<BusinessError, Unit>>()()
        }
    }

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
        val courier = Courier.of(CourierType.WALKING, 1, LocationTestData.random()).shouldBeRight()
        every { courierRepository.getCouriersWithAssignedOrders() } returns listOf(courier)
        every { orderRepository.findAllAssigned() } returns emptyList()

        // Act
        val result = sut.execute().shouldBeLeft()

        // Assert
        result shouldBe MovementError.NoOrders
    }

    @Test
    fun `move courier one tick towards order`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.restore(10, 10), 1).shouldBeRight()
        val courier = Courier.of(CourierType.WALKING, speed = 4, Location.restore(1, 1)).shouldBeRight()

        order.assignToCourier(courier.id)

        every { courierRepository.getCouriersWithAssignedOrders() } returns listOf(courier)
        every { orderRepository.findAllAssigned() } returns listOf(order)

        // Act
        sut.execute().shouldBeRight()

        // Assert
        courier.location shouldBe Location.restore(5, 1)
        order.status shouldBe OrderStatus.ASSIGNED

        verify { courierRepository.track(courier) }
        verify { orderRepository.track(order) }
        verify { unitOfWork.commit() }
    }

    @Test
    fun `move courier until delivery is done`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.restore(10, 10), 1).shouldBeRight()
        val courier = Courier.of(CourierType.WALKING, 4, Location.restore(1, 1)).shouldBeRight()
        order.assignToCourier(courier.id)

        every { courierRepository.getCouriersWithAssignedOrders() } returns listOf(courier)
        every { orderRepository.findAllAssigned() } returns listOf(order)

        val ticks = courier.calculateTimeToLocation(order.location)

        // Act
        repeat(ticks) {
            sut.execute().shouldBeRight()
        }

        // Assert
        courier.location shouldBe order.location
        order.status shouldBe OrderStatus.COMPLETED
        verify(exactly = ticks) { unitOfWork.commit() }
    }
}
