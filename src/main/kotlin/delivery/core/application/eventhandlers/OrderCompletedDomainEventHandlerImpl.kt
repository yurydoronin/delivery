package delivery.core.application.eventhandlers

import delivery.core.application.ports.output.MessageBusProducerPort
import delivery.core.domain.model.order.events.OrderCompletedDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class OrderCompletedDomainEventHandlerImpl(
    private val producer: MessageBusProducerPort
) : OrderCompletedDomainEventHandler {

    @EventListener
    override fun handle(event: OrderCompletedDomainEvent) {
        producer.publishOrderCompleted(event)
    }
}