package delivery.infrastructure.output.adapters.postgres.outbox

import java.sql.Timestamp
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
class OutboxRepository(
    private val jdbcClient: JdbcClient
) {
    internal fun save(message: OutboxMessage) {
        jdbcClient.sql(
            """
            INSERT INTO outbox (
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
}
