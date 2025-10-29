package common.types.base

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Clock
import java.time.Instant
import java.util.UUID
import org.springframework.context.ApplicationEvent

abstract class DomainEvent(
    // source [источник] - объект, с которым связано событие, то есть кто породил событие.
    // Неправильно передавать весь агрегат [сам объект] внутри доменного события -> сильная связанность между событием и агрегатом.
    source: Any,
    val eventId: UUID = UUID.randomUUID(),
    val occurredOnUtc: Instant = Instant.now(Clock.systemUTC())
) : ApplicationEvent(source) {

    @JsonIgnore
    override fun getSource(): Any = super.getSource()
}