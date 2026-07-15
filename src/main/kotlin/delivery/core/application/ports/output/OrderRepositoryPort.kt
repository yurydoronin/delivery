package delivery.core.application.ports.output

import delivery.core.domain.model.order.Order
import java.util.UUID

interface OrderRepositoryPort {

    fun track(order: Order)
    fun findById(orderId: UUID): Order?
    fun findAllAssigned(): List<Order>
}
