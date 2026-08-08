package delivery.application.dispatcher

import arrow.core.Either
import delivery.application.eventhandlers.DomainEventHandlerRegistry
import delivery.application.ports.output.DomainEventOutboxPort
import delivery.application.ports.output.atomic.AtomicOperationPort
import delivery.application.ports.output.atomic.TransactionPropagation
import delivery.application.ports.output.outbox.OutboxMessage
import delivery.common.types.base.DomainEvent
import delivery.common.types.error.BusinessError
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class DomainEventProcessor(
    private val objectMapper: ObjectMapper,
    private val handlers: DomainEventHandlerRegistry,
    private val outboxRepository: DomainEventOutboxPort,
    private val atomicOperation: AtomicOperationPort,
) {
    fun process(message: OutboxMessage): Either<BusinessError, Unit> {
        val eventClass = Class.forName(message.eventType)
        val eventObject = objectMapper.readValue(message.payload, eventClass) as? DomainEvent
            ?: throw IllegalStateException("Invalid outbox message type: $eventClass")

        return atomicOperation.execute(
            propagation = TransactionPropagation.REQUIRES_NEW
        ) {
            handlers.handle(eventObject)
                .map {
                    message.markAsProcessed()
                    outboxRepository.markProcessed(message)
                }
        }
    }
}
