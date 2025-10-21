package common.types.base

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Clock
import java.time.Instant
import java.util.UUID
import org.springframework.context.ApplicationEvent

abstract class DomainEvent(
    source: Any,
    val eventId: UUID = UUID.randomUUID(),
    val occurredOnUtc: Instant = Instant.now(Clock.systemUTC())
) : ApplicationEvent(source) {

    @JsonIgnore
    override fun getSource(): Any = super.getSource()
}