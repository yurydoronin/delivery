package delivery.infrastructure.output.adapters.postgres.outbox

import java.sql.Timestamp
import java.util.UUID
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
class OutboxRepository(
    private val jdbcClient: JdbcClient
) {

    fun findUnprocessedMessages(): List<OutboxMessage> =
        jdbcClient.sql(
            """
            SELECT
                id,
                event_type,
                aggregate_id,
                aggregate_type,
                payload,
                occurred_on_utc,
                processed_on_utc
            FROM outbox
            WHERE processed_on_utc IS NULL
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

    internal fun save(message: OutboxMessage) {
        // Проверяем, есть ли дата обработки.
        // Если сообщение новое (только что создано в памяти), processedOnUtc у него null
        if (message.processedOnUtc() == null) {
            insert(message)
        } else {
            update(message)
        }
    }

    private fun insert(message: OutboxMessage) {
        jdbcClient.sql(
            """
            INSERT INTO outbox (
                id, 
                event_type, 
                aggregate_id, 
                aggregate_type, 
                payload, 
                occurred_on_utc,
                processed_on_utc
            )
            VALUES (:id, :eventType, :aggregateId, :aggregateType, :payload, :occurredOnUtc, NULL)
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

    private fun update(message: OutboxMessage) {
        val updated = jdbcClient.sql(
            """
            UPDATE outbox
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

        check(updated == 1) {
            "Outbox message ${message.id} not found for update"
        }
    }
}
