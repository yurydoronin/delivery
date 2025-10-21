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
    private val aggregateTracker: AggregateTracker,
    private val repository: OrderJpaRepository
) : OrderRepositoryPort {

    override fun track(order: Order) {
        aggregateTracker.track(order)
    }

    override fun get(orderId: UUID): Order? = repository.findByIdOrNull(orderId)

    override fun findAnyCreated(): Order? =
        repository.findFirstBy_status(OrderStatus.CREATED)

    override fun findAllAssigned(): List<Order> =
        repository.findAllByStatus(OrderStatus.ASSIGNED)
}
