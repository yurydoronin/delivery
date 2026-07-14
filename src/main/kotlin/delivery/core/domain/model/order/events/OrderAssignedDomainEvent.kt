package delivery.core.domain.model.order.events

import common.types.base.DomainEvent
import java.util.UUID

data class OrderAssignedDomainEvent(
    val orderId: UUID,
    val courierId: UUID
) : DomainEvent()
