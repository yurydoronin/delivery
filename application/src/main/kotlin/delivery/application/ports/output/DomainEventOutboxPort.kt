package delivery.application.ports.output

import delivery.application.ports.output.outbox.OutboxMessage

interface DomainEventOutboxPort {
    fun save(message: OutboxMessage)
    fun findUnprocessedMessages(): List<OutboxMessage>
    fun markProcessed(message: OutboxMessage)
}
