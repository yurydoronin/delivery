package delivery.application.eventhandlers

import delivery.application.ports.input.commands.AssignOrderUseCase
import delivery.domain.model.order.events.OrderCreatedDomainEvent
import org.springframework.stereotype.Service

@Service
class OrderCreatedDomainEventHandler(
    private val assignCourierUseCase: AssignOrderUseCase
) : DomainEventHandler<OrderCreatedDomainEvent> {

    override fun handle(event: OrderCreatedDomainEvent) {
        assignCourierUseCase.execute(event.orderId)
    }
}
