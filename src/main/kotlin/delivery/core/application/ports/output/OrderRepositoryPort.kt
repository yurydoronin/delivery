package delivery.core.application.ports.output

import delivery.core.domain.model.order.Order
import java.util.UUID

interface OrderRepositoryPort {

    fun track(order: Order)
    fun get(orderId: UUID): Order?
    fun findAnyCreated(): Order?
    fun findAllAssigned(): List<Order>
}
