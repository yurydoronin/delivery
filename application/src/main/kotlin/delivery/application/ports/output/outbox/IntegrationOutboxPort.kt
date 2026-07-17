package delivery.application.ports.output.outbox

interface IntegrationOutboxPort {
    fun save(message: OutboxMessage)
}