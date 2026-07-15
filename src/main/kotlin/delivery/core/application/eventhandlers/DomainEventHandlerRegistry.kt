package delivery.core.application.eventhandlers

import common.types.base.DomainEvent
import delivery.core.domain.model.order.events.OrderCreatedDomainEvent
import org.springframework.stereotype.Component

@Component
class DomainEventHandlerRegistry(
    private val orderCreatedHandler: OrderCreatedDomainEventHandler,
//    private val orderCompletedHandler: OrderCompletedDomainEventHandler
) {


    fun handle(event: DomainEvent) {

        when(event) {

            is OrderCreatedDomainEvent ->
                orderCreatedHandler.handle(event)

//            is OrderCompletedDomainEvent ->
//                orderCompletedHandler.handle(event)

        }
    }
}