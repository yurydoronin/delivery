package delivery.application.ports.output.outbox

import java.time.Clock
import java.time.Instant
import java.util.UUID

data class OutboxMessage(
    val id: UUID,
    val eventType: String,
    val aggregateId: UUID,
    val aggregateType: String,
    val payload: String,
    val occurredOnUtc: Instant = Instant.now(Clock.systemUTC()),
) {
    private var processedOnUtc: Instant? = null

    fun markAsProcessed() {
        processedOnUtc = Instant.now(Clock.systemUTC())
    }

    fun processedOnUtc(): Instant? = processedOnUtc

    companion object {
        fun restore(
            id: UUID,
            eventType: String,
            aggregateId: UUID,
            aggregateType: String,
            payload: String,
            occurredOnUtc: Instant,
        ) = OutboxMessage(
            id = id,
            eventType = eventType,
            aggregateId = aggregateId,
            aggregateType = aggregateType,
            payload = payload,
            occurredOnUtc = occurredOnUtc,
        )
    }
}