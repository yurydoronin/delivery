package delivery.core.domain.model.order.events

import common.types.base.DomainEvent
import delivery.core.domain.model.order.Order
import java.util.UUID

class OrderCompletedDomainEvent(
    order: Order
) : DomainEvent(order) {
    val orderId: UUID = order.id
    val courierId: UUID? = order.courierId
}
