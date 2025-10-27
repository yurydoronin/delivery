package delivery.core.domain.model.order.events

import common.types.base.DomainEvent
import java.util.UUID

data class OrderCreatedDomainEvent(
    val orderId: UUID
) : DomainEvent(source = orderId)