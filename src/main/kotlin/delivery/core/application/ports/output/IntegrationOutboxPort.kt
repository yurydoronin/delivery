package delivery.core.application.ports.output

import delivery.infrastructure.output.adapters.postgres.outbox.OutboxMessage

interface IntegrationOutboxPort {
    fun save(message: OutboxMessage)
}
