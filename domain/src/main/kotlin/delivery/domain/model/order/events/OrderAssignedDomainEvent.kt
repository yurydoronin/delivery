package delivery.domain.model.order.events

import delivery.common.types.base.DomainEvent
import java.util.UUID

data class OrderAssignedDomainEvent(
    val orderId: UUID,
    val courierId: UUID
) : DomainEvent()
