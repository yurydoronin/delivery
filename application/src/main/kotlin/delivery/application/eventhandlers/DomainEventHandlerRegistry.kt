package delivery.application.eventhandlers

import delivery.common.types.base.DomainEvent
import delivery.domain.model.order.events.OrderCreatedDomainEvent
import org.springframework.stereotype.Component

@Component
class DomainEventHandlerRegistry(
    private val orderCreatedHandler: OrderCreatedDomainEventHandler,
) {

    fun handle(event: DomainEvent) {
        when (event) {
            is OrderCreatedDomainEvent -> orderCreatedHandler.handle(event)
        }
    }
}
