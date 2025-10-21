package delivery.core.application.eventhandlers

import delivery.core.application.ports.output.MessageBusProducerPort
import delivery.core.domain.model.order.events.OrderCreatedDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class OrderCreatedDomainEventHandlerImpl(
    private val producer: MessageBusProducerPort
) : OrderCreatedDomainEventHandler {

    @EventListener
    override fun handle(event: OrderCreatedDomainEvent) {
        producer.publish(event)
    }
}
