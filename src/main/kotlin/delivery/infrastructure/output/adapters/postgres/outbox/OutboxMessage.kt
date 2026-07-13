package delivery.infrastructure.output.adapters.postgres.outbox

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
)
