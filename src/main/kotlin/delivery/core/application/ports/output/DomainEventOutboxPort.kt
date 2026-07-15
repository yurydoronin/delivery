package delivery.core.application.ports.output

import delivery.infrastructure.output.adapters.postgres.outbox.OutboxMessage

interface DomainEventOutboxPort {
    fun save(message: OutboxMessage)
    fun findUnprocessedMessages(): List<OutboxMessage>
    fun markProcessed(message: OutboxMessage)
}
