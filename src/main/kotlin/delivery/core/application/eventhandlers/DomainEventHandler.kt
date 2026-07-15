package delivery.core.application.eventhandlers

import common.types.base.DomainEvent

interface DomainEventHandler<T : DomainEvent> {
    fun handle(event: T)
}
