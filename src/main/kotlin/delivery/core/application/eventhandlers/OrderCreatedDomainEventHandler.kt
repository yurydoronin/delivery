package delivery.core.application.eventhandlers

import delivery.core.domain.model.order.events.OrderCreatedDomainEvent

interface OrderCreatedDomainEventHandler {
    fun handle(event: OrderCreatedDomainEvent)
}