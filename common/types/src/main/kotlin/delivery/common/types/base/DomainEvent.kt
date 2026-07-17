package delivery.common.types.base

import com.github.f4b6a3.uuid.UuidCreator
import java.time.Clock
import java.time.Instant
import java.util.UUID

abstract class DomainEvent(
    val eventId: UUID = UuidCreator.getTimeOrderedEpoch(),
    val occurredOnUtc: Instant = Instant.now(Clock.systemUTC())
)
