package delivery.infrastructure.output.adapters.postgres.outbox

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OutboxRepository : JpaRepository<OutboxMessage, UUID> {

    @Query("SELECT om FROM OutboxMessage om WHERE om.processedOnUtc IS NULL")
    fun findUnprocessedMessages(): List<OutboxMessage>
}