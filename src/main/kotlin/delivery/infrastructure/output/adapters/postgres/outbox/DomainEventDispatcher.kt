package delivery.infrastructure.output.adapters.postgres.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import common.types.base.DomainEvent
import delivery.core.application.eventhandlers.DomainEventHandlerRegistry
import delivery.core.application.ports.output.DomainEventOutboxPort
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DomainEventDispatcher(
    private val handlers: DomainEventHandlerRegistry,
    private val repository: DomainEventOutboxPort,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(DomainEventDispatcher::class.java)

    @Scheduled(fixedDelay = 500)
    @Transactional
    fun dispatch() {
        repository.findUnprocessedMessages()
            .takeIf { it.isNotEmpty() }
            ?.forEach { outboxMessage ->
                runCatching {
                    val eventClass = Class.forName(outboxMessage.eventType)
                    val eventObject = objectMapper.readValue(outboxMessage.payload, eventClass) as? DomainEvent
                        ?: throw IllegalStateException("Invalid outbox message type: $eventClass")

                    handlers.handle(eventObject)
                    outboxMessage.markAsProcessed()
                    repository.markProcessed(outboxMessage)
                }.onFailure { e ->
                    log.error("Failed to publish outbox message", e)
                    throw e // иначе транзакция не откатится
                }
            } ?: log.debug("No unprocessed outbox messages")
    }
}