package delivery.domain.model.order.events

import delivery.common.types.base.DomainEvent
import java.util.UUID

data class OrderCreatedDomainEvent(
    val orderId: UUID
) : DomainEvent()
