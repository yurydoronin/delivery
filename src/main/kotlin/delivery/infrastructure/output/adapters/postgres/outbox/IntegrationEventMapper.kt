package delivery.infrastructure.output.adapters.postgres.outbox

import common.types.base.DomainEvent
import delivery.core.domain.model.order.events.OrderCompletedDomainEvent
import delivery.core.domain.model.order.events.OrderCreatedDomainEvent
import java.time.Instant
import java.util.UUID

data class OrderCreatedOutboxPayload(
    val eventId: UUID,
    val orderId: UUID,
    val occurredAt: Instant
)

data class OrderCompletedOutboxPayload(
    val eventId: UUID,
    val orderId: UUID,
    val courierId: UUID,
    val occurredAt: Instant
)

internal fun DomainEvent.toIntegrationEventPayload(): Any =
    when (this) {
        is OrderCreatedDomainEvent ->
            OrderCreatedOutboxPayload(
                eventId = eventId,
                orderId = orderId,
                occurredAt = occurredOnUtc
            )

        is OrderCompletedDomainEvent ->
            OrderCompletedOutboxPayload(
                eventId = eventId,
                orderId = orderId,
                courierId = courierId,
                occurredAt = occurredOnUtc
            )

        else -> error("Unsupported domain event: ${this::class.simpleName}")
    }
