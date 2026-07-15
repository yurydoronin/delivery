//package delivery.core.application.eventhandlers
//
//import delivery.core.application.ports.input.commands.CouriersMovementUseCase
//import delivery.core.application.ports.output.MessageBusProducerPort
//import delivery.core.domain.model.order.events.OrderAssignedDomainEvent
//import delivery.core.domain.model.order.events.OrderCompletedDomainEvent
//import delivery.core.domain.model.order.events.OrderCreatedDomainEvent
//import org.springframework.context.event.EventListener
//import org.springframework.stereotype.Service
//
//@Service
//class OrderCompletedDomainEventHandler(
//    private val couriersMovementUseCase: CouriersMovementUseCase
//) : DomainEventHandler<OrderAssignedDomainEvent> {
//
//    override fun handle(event: OrderCompletedDomainEvent) {
//        couriersMovementUseCase.execute(event.orderId)
//    }
//}