package delivery.infrastructure.output.adapters.postgres

import com.ninjasquad.springmockk.MockkBean
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
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired

class OrderRepositoryTest @Autowired constructor(
    private val unitOfWork: UnitOfWork,
    private val orderRepository: OrderRepository,
    private val courierRepository: CourierRepository
) : BaseRepositoryTest() {
    @MockkBean(relaxed = true)
    lateinit var aggregateTracker: AggregateTracker

    @Test
    fun `save order`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.of(1, 1), 5)

        // Act
        orderRepository.save(order)
        unitOfWork.commit()

        // Assert
        val saved = orderRepository.get(order.id)
        saved shouldNotBe null
        saved!!.volume shouldBe 5
        verify { aggregateTracker.track(order) }
    }

    @Test
    fun `add new order`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.of(2, 2), 3)

        // Act
        orderRepository.add(order)
        unitOfWork.commit()

        // Assert
        val newOrder = orderRepository.get(order.id)
        newOrder shouldNotBe null
        newOrder!!.volume shouldBe 3
        verify { aggregateTracker.track(order) }
    }

    @Test
    fun `update existing order`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.of(1, 1), 2)
        orderRepository.add(order)
        val courier = Courier.of("Вася", 2, Location.of(2, 2))
        courierRepository.add(courier)
        unitOfWork.commit()
        order.assignToCourier(courier.id)

        // Act
        orderRepository.update(order)
        unitOfWork.commit()

        // Assert
        val updatedOrder = orderRepository.get(order.id)
        updatedOrder shouldNotBe null
        updatedOrder!!.status shouldBe OrderStatus.ASSIGNED
        updatedOrder.courierId shouldBe courier.id
        verify { aggregateTracker.track(order) }
    }

    @Test
    fun `get order by id`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.of(1, 1), 10)
        orderRepository.save(order)
        unitOfWork.commit()

        // Act
        val found = orderRepository.get(order.id)

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
        orderRepository.add(order1)
        orderRepository.add(order2)
        unitOfWork.commit()

        // Act
        val createdOrder = orderRepository.findAnyCreated()

        // Assert
        createdOrder shouldNotBe null
        createdOrder!!.status shouldBe OrderStatus.CREATED
        verify { aggregateTracker.track(order1) }
        verify { aggregateTracker.track(order2) }
    }

    @Test
    fun `find all assigned orders`() {
        // Arrange
        val order1 = Order.of(UUID.randomUUID(), Location.of(1, 1), 1)
        val order2 = Order.of(UUID.randomUUID(), Location.of(2, 2), 2)
        val order3 = Order.of(UUID.randomUUID(), Location.of(3, 3), 3)
        orderRepository.add(order1)
        orderRepository.add(order2)
        orderRepository.add(order3)
        unitOfWork.commit()

        val courier1 = Courier.of("Вася", 2, Location.of(2, 2))
        val courier2 = Courier.of("Петя", 2, Location.of(5, 5))
        courierRepository.add(courier1)
        courierRepository.add(courier2)
        unitOfWork.commit()
        order1.assignToCourier(courier1.id)
        order2.assignToCourier(courier2.id)
        orderRepository.update(order1)
        orderRepository.update(order2)
        unitOfWork.commit()

        // Act
        val assignedOrders = orderRepository.findAllAssigned()

        // Assert
        assignedOrders.shouldHaveSize(2)
        assignedOrders.map { it.id }.shouldContainAll(order1.id, order2.id)
        assignedOrders.forEach { it.status shouldBe OrderStatus.ASSIGNED }
        verify { aggregateTracker.track(order1) }
        verify { aggregateTracker.track(order2) }
        verify { aggregateTracker.track(order3) }
    }
}