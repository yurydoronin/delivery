package delivery.application.eventhandlers

import arrow.core.Either
import arrow.core.raise.either
import delivery.common.types.base.DomainEvent
import delivery.common.types.error.BusinessError
import delivery.domain.model.order.events.OrderCreatedDomainEvent
import org.springframework.stereotype.Component

@Component
class DomainEventHandlerRegistry(
    private val orderCreatedHandler: OrderCreatedDomainEventHandler,
) {

    fun handle(event: DomainEvent): Either<BusinessError, Unit> = either {
        when (event) {
            is OrderCreatedDomainEvent -> orderCreatedHandler.handle(event).bind()
        }
    }
}
