package delivery

import common.types.base.DomainEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class DomainEventPublisher(
    private val publisher: ApplicationEventPublisher
) {
    fun publish(event: DomainEvent) {
        publisher.publishEvent(event)
    }
}