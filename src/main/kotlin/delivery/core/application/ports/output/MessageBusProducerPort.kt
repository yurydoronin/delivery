package delivery.core.application.ports.output

import common.types.base.DomainEvent

interface MessageBusProducerPort {
    fun <E : DomainEvent> publish(event: E)
}