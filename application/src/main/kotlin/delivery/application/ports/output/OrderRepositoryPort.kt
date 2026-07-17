package delivery.application.ports.output

import delivery.domain.model.order.Order
import java.util.UUID

interface OrderRepositoryPort {

    fun track(order: Order)
    fun findById(orderId: UUID): Order?
    fun findAllAssigned(): List<Order>
}
