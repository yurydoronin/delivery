package delivery.infrastructure.output.adapters.postgres.outbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "outbox")
data class OutboxMessage(
    @Id
    val id: UUID,
    @Column(name = "event_type")
    val eventType: String,
    @Column(name = "aggregate_id")
    val aggregateId: UUID,
    @Column(name = "aggregate_type")
    val aggregateType: String,
    @Column(columnDefinition = "text")
    val payload: String,
    @Column(name = "occurred_on_utc")
    val occurredOnUtc: Instant = Instant.now(Clock.systemUTC())
) {
    @Column(name = "processed_on_utc")
    private var processedOnUtc: Instant? = null

    fun markAsProcessed() {
        processedOnUtc = Instant.now(Clock.systemUTC())
    }
}