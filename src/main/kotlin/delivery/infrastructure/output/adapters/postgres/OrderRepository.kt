package delivery.infrastructure.output.adapters.postgres

import delivery.core.application.ports.output.AggregateTracker
import delivery.core.application.ports.output.OrderRepositoryPort
import delivery.core.domain.model.order.Order
import delivery.core.domain.model.order.OrderStatus
import java.util.UUID
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class OrderRepository(
    val aggregateTracker: AggregateTracker,
    val repository: OrderJpaRepository
) : OrderRepositoryPort {

    override fun save(order: Order) {
        aggregateTracker.track(order)
        repository.save(order)
    }

    override fun add(order: Order) {
        require(!repository.existsById(order.id)) {
            "Order with id ${order.id} already exists"
        }
        aggregateTracker.track(order)
        repository.save(order)
    }

    override fun update(order: Order) {
        require(repository.existsById(order.id)) {
            "Cannot update non-existent order with id ${order.id}"
        }
        aggregateTracker.track(order)
        repository.save(order)
    }

    override fun get(orderId: UUID): Order? = repository.findByIdOrNull(orderId)

    override fun findAnyCreated(): Order? =
        repository.findFirstByStatus(OrderStatus.CREATED)

    override fun findAllAssigned(): List<Order> =
        repository.findAllByStatus(OrderStatus.ASSIGNED)

}
