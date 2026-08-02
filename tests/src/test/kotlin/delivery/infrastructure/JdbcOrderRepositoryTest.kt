package delivery.infrastructure

import com.ninjasquad.springmockk.MockkBean
import delivery.BaseRepositoryTest
import delivery.application.ports.output.AggregateTracker
import delivery.application.ports.output.UnitOfWork
import delivery.domain.kernel.LocationTestData
import delivery.domain.model.courier.Courier
import delivery.domain.model.courier.CourierType
import delivery.domain.model.order.Order
import delivery.domain.model.order.OrderStatus
import delivery.infrastructure.output.adapters.postgres.JdbcCourierRepository
import delivery.infrastructure.output.adapters.postgres.JdbcOrderRepository
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired

class JdbcOrderRepositoryTest @Autowired constructor(
    private val unitOfWork: UnitOfWork,
    private val jdbcOrderRepository: JdbcOrderRepository,
    private val jdbcCourierRepository: JdbcCourierRepository
) : BaseRepositoryTest() {

    @MockkBean(relaxed = true)
    lateinit var aggregateTracker: AggregateTracker

    @Test
    fun `add new order`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), LocationTestData.random(), 3).shouldBeRight()

        // Act
        jdbcOrderRepository.track(order)
        every { aggregateTracker.getTracked() } returns listOf(order)
        unitOfWork.commit()

        // Assert
        val newOrder = jdbcOrderRepository.findById(order.id)
        newOrder shouldNotBe null
        newOrder!!.volume shouldBeExactly 3
        verify { aggregateTracker.track(order) }

    }

    @Test
    fun `updates order status from created to assigned`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), LocationTestData.random(), 2).shouldBeRight()
        val courier = Courier.of(CourierType.WALKING, 2, LocationTestData.random()).shouldBeRight()

        every { aggregateTracker.getTracked() } returns listOf(order, courier)
        unitOfWork.commit()
        order.assignToCourier(courier.id)

        // Act
        jdbcOrderRepository.track(order)
        every { aggregateTracker.getTracked() } returns listOf(order)
        unitOfWork.commit()

        // Assert
        val updatedOrder = jdbcOrderRepository.findById(order.id)
        updatedOrder shouldNotBe null
        updatedOrder!!.status shouldBe OrderStatus.ASSIGNED
        updatedOrder.courierId shouldBe courier.id
        verify { aggregateTracker.track(order) }
    }

    @Test
    fun `get order by id`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), LocationTestData.random(), 10).shouldBeRight()
        jdbcOrderRepository.track(order)
        every { aggregateTracker.getTracked() } returns listOf(order)
        unitOfWork.commit()

        // Act
        val found = jdbcOrderRepository.findById(order.id)

        // Assert
        found shouldNotBe null
        found!!.id shouldBe order.id
        found.status shouldBe OrderStatus.CREATED
        verify { aggregateTracker.track(order) }
    }

    @Test
    fun `find all assigned orders`() {
        // Arrange
        val order1 = Order.of(UUID.randomUUID(), LocationTestData.random(), 1).shouldBeRight()
        val order2 = Order.of(UUID.randomUUID(), LocationTestData.random(), 2).shouldBeRight()
        val order3 = Order.of(UUID.randomUUID(), LocationTestData.random(), 3).shouldBeRight()
        jdbcOrderRepository.track(order1)
        jdbcOrderRepository.track(order2)
        jdbcOrderRepository.track(order3)
        every { aggregateTracker.getTracked() } returns listOf(order1, order2, order3)
        unitOfWork.commit()

        val courier1 = Courier.of(CourierType.WALKING, 2, LocationTestData.random()).shouldBeRight()
        val courier2 = Courier.of(CourierType.WALKING, 2, LocationTestData.random()).shouldBeRight()
        jdbcCourierRepository.track(courier1)
        jdbcCourierRepository.track(courier2)
        every { aggregateTracker.getTracked() } returns listOf(courier1, courier2)
        unitOfWork.commit()
        order1.assignToCourier(courier1.id)
        order2.assignToCourier(courier2.id)
        jdbcOrderRepository.track(order1)
        jdbcOrderRepository.track(order2)
        every { aggregateTracker.getTracked() } returns listOf(order1, order2)
        unitOfWork.commit()

        // Act
        val assignedOrders = jdbcOrderRepository.findAllAssigned()

        // Assert
        assignedOrders.shouldHaveSize(2)
        assignedOrders.map { it.id }.shouldContainAll(order1.id, order2.id)
        assignedOrders.forEach { it.status shouldBe OrderStatus.ASSIGNED }
        verify { aggregateTracker.track(order1) }
        verify { aggregateTracker.track(order2) }
        verify { aggregateTracker.track(order3) }
    }
}
