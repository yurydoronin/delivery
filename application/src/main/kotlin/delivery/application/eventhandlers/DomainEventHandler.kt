package delivery.application.eventhandlers

import arrow.core.Either
import delivery.common.types.base.DomainEvent
import delivery.common.types.error.BusinessError

interface DomainEventHandler<T : DomainEvent> {
    fun handle(event: T): Either<BusinessError, Unit>
}
