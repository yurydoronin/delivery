package delivery.infrastructure.output.adapters.postgres.outbox

import delivery.core.application.ports.output.IntegrationOutboxPort
import java.sql.Timestamp
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
class JdbcIntegrationOutboxRepository(
    private val jdbcClient: JdbcClient
) : IntegrationOutboxPort {

    override fun save(message: OutboxMessage) {
        jdbcClient.sql(
            """
            INSERT INTO outbox_integration (
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
