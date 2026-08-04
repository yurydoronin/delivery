package delivery.application.eventhandlers

import arrow.core.Either
import arrow.core.raise.either
import delivery.application.ports.input.commands.AssignOrderUseCase
import delivery.common.types.error.BusinessError
import delivery.domain.model.order.events.OrderCreatedDomainEvent
import org.springframework.stereotype.Component

@Component
class OrderCreatedDomainEventHandler(
    private val assignCourierUseCase: AssignOrderUseCase
) : DomainEventHandler<OrderCreatedDomainEvent> {

    override fun handle(event: OrderCreatedDomainEvent): Either<BusinessError, Unit> = either {
        assignCourierUseCase.execute(event.orderId).bind()
    }
}
