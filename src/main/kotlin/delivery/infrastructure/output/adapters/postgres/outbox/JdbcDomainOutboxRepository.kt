package delivery.infrastructure.output.adapters.postgres.outbox

import delivery.core.application.ports.output.DomainEventOutboxPort
import java.sql.Timestamp
import java.util.UUID
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
class JdbcDomainOutboxRepository(
    private val jdbcClient: JdbcClient
) : DomainEventOutboxPort {

    override fun save(message: OutboxMessage) {
        jdbcClient.sql(
            """
            INSERT INTO outbox_domain (
                id, 
                event_type, 
                aggregate_id, 
                aggregate_type, 
                payload, 
                occurred_on_utc
            )
            VALUES (:id, :eventType, :aggregateId, :aggregateType, :payload, :occurredOnUtc)
            """.trimIndent()
        )
            .param("id", message.id)
            .param("eventType", message.eventType)
            .param("aggregateId", message.aggregateId)
            .param("aggregateType", message.aggregateType)
            .param("payload", message.payload)
            .param("occurredOnUtc", Timestamp.from(message.occurredOnUtc))
            .update()
    }

    override fun findUnprocessedMessages(): List<OutboxMessage> {
        return jdbcClient.sql(
            """
            SELECT *
            FROM outbox_domain
            WHERE processed_on_utc IS NULL
            ORDER BY occurred_on_utc
            LIMIT 100
            FOR UPDATE SKIP LOCKED
            """.trimIndent()
        )
            .query { rs, _ ->
                OutboxMessage.restore(
                    id = rs.getObject("id", UUID::class.java),
                    eventType = rs.getString("event_type"),
                    aggregateId = rs.getObject("aggregate_id", UUID::class.java),
                    aggregateType = rs.getString("aggregate_type"),
                    payload = rs.getString("payload"),
                    occurredOnUtc = rs.getTimestamp("occurred_on_utc").toInstant()
                )
            }
            .list()
    }

    override fun markProcessed(message: OutboxMessage) {
        jdbcClient.sql(
            """
            UPDATE outbox_domain
            SET processed_on_utc = :processedOnUtc
            WHERE id = :id
            """.trimIndent()
        )
            .param("id", message.id)
            .param(
                "processedOnUtc", message.processedOnUtc()
                    ?.let { Timestamp.from(it) }
            )
            .update()
    }
}