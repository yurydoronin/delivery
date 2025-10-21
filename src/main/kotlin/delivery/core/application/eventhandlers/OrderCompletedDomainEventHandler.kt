package delivery.core.application.eventhandlers

import delivery.core.domain.model.order.events.OrderCompletedDomainEvent

interface OrderCompletedDomainEventHandler {
    fun handle(event: OrderCompletedDomainEvent)
}