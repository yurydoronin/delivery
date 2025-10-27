package delivery.infrastructure.output.adapters.postgres.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import common.types.base.DomainEvent
import delivery.DomainEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class OutboxPublisher(
    private val publisher: DomainEventPublisher,
    private val repository: OutboxRepository,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(OutboxPublisher::class.java)

    @Scheduled(fixedDelay = 1000)
    fun publishPendingEvents() {
        repository.findUnprocessedMessages()
            .takeIf { it.isNotEmpty() }
            ?.forEach { outboxMessage ->
                runCatching {
                    val eventClass = Class.forName(outboxMessage.eventType)
                    val eventObject = objectMapper.readValue(outboxMessage.payload, eventClass) as? DomainEvent
                        ?: throw IllegalStateException("Invalid outbox message type: $eventClass")

                    publisher.publish(eventObject)
                    outboxMessage.markAsProcessed()
                    repository.save(outboxMessage)
                }.onFailure { ex ->
                    log.error("Failed to publish outbox message", ex)
                }
            } ?: log.debug("No unprocessed outbox messages")
    }
}