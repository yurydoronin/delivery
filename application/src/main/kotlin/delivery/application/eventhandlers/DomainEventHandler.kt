package delivery.application.eventhandlers

import delivery.common.types.base.DomainEvent

interface DomainEventHandler<T : DomainEvent> {
    fun handle(event: T)
}
