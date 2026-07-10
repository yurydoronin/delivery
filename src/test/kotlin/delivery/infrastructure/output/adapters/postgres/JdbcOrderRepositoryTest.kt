package delivery.infrastructure.output.adapters.postgres

import arrow.core.raise.either
import com.ninjasquad.springmockk.MockkBean
import delivery.DomainEventPublisher
import delivery.core.application.ports.output.AggregateTracker
import delivery.core.application.ports.output.UnitOfWork
import delivery.core.domain.kernel.Location
import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.order.Order
import delivery.core.domain.model.order.OrderStatus
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
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

    @MockkBean(relaxed = true)
    lateinit var publisher: DomainEventPublisher

    @Test
    fun `add new order`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.of(2, 2), 3)

        // Act
        jdbcOrderRepository.track(order)
        every { aggregateTracker.getTracked() } returns listOf(order)
        unitOfWork.commit()

        // Assert
        val newOrder = jdbcOrderRepository.get(order.id)
        newOrder shouldNotBe null
        newOrder!!.volume shouldBe 3
        verify { aggregateTracker.track(order) }
    }

    @Test
    fun `update existing order`() {
        either {
            // Arrange
            val order = Order.of(UUID.randomUUID(), Location.of(1, 1), 2)
            jdbcOrderRepository.track(order)
            val courier = Courier.of("Вася", 2, Location.of(2, 2)).bind()
            jdbcCourierRepository.track(courier)
            every { aggregateTracker.getTracked() } returns listOf(order, courier)
            unitOfWork.commit()
            order.assignToCourier(courier.id)

            // Act
            jdbcOrderRepository.track(order)
            every { aggregateTracker.getTracked() } returns listOf(order)
            unitOfWork.commit()

            // Assert
            val updatedOrder = jdbcOrderRepository.get(order.id)
            updatedOrder shouldNotBe null
            updatedOrder!!.status shouldBe OrderStatus.ASSIGNED
            updatedOrder.courierId shouldBe courier.id
            verify { aggregateTracker.track(order) }
        }
    }

    @Test
    fun `get order by id`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.of(1, 1), 10)
        jdbcOrderRepository.track(order)
        every { aggregateTracker.getTracked() } returns listOf(order)
        unitOfWork.commit()

        // Act
        val found = jdbcOrderRepository.get(order.id)

        // Assert
        found shouldNotBe null
        found!!.id shouldBe order.id
        found.status shouldBe OrderStatus.CREATED
        verify { aggregateTracker.track(order) }
    }

    @Test
    fun `find any created order`() {
        // Arrange
        val order1 = Order.of(UUID.randomUUID(), Location.of(1, 1), 1)
        val order2 = Order.of(UUID.randomUUID(), Location.of(2, 2), 2)
        jdbcOrderRepository.track(order1)
        jdbcOrderRepository.track(order2)
        every { aggregateTracker.getTracked() } returns listOf(order1, order2)
        unitOfWork.commit()

        // Act
        val createdOrder = jdbcOrderRepository.findAnyCreatedForUpdate()

        // Assert
        createdOrder shouldNotBe null
        createdOrder!!.status shouldBe OrderStatus.CREATED
        verify { aggregateTracker.track(order1) }
        verify { aggregateTracker.track(order2) }
    }

    @Test
    fun `find all assigned orders`() {
        either {
            // Arrange
            val order1 = Order.of(UUID.randomUUID(), Location.of(1, 1), 1)
            val order2 = Order.of(UUID.randomUUID(), Location.of(2, 2), 2)
            val order3 = Order.of(UUID.randomUUID(), Location.of(3, 3), 3)
            jdbcOrderRepository.track(order1)
            jdbcOrderRepository.track(order2)
            jdbcOrderRepository.track(order3)
            every { aggregateTracker.getTracked() } returns listOf(order1, order2, order3)
            unitOfWork.commit()

            val courier1 = Courier.of("Вася", 2, Location.of(2, 2)).bind()
            val courier2 = Courier.of("Петя", 2, Location.of(5, 5)).bind()
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
            val assignedOrders = jdbcOrderRepository.findAllAssignedForUpdate()

            // Assert
            assignedOrders.shouldHaveSize(2)
            assignedOrders.map { it.id }.shouldContainAll(order1.id, order2.id)
            assignedOrders.forEach { it.status shouldBe OrderStatus.ASSIGNED }
            verify { aggregateTracker.track(order1) }
            verify { aggregateTracker.track(order2) }
            verify { aggregateTracker.track(order3) }
        }
    }
}