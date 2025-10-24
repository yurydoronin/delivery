package delivery.core.application.ports.output

import delivery.core.domain.model.order.events.OrderCompletedDomainEvent
import delivery.core.domain.model.order.events.OrderCreatedDomainEvent

interface MessageBusProducerPort {
    fun publishOrderCreated(event: OrderCreatedDomainEvent)
    fun publishOrderCompleted(event: OrderCompletedDomainEvent)
}