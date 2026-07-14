package delivery.infrastructure.output.adapters.postgres.outbox

import com.google.protobuf.MessageOrBuilder
import common.types.base.DomainEvent
import delivery.core.domain.model.order.events.OrderAssignedDomainEvent
import delivery.core.domain.model.order.events.OrderCompletedDomainEvent
import queues.order.events.OrderEventsProto

internal fun DomainEvent.integrationEventType(): String =
    when (this) {
        is OrderAssignedDomainEvent -> "OrderAssignedIntegrationEvent"
        is OrderCompletedDomainEvent -> "OrderCompletedIntegrationEvent"

        else -> error("Unsupported integration event: ${this::class.simpleName}")
    }

internal fun DomainEvent.isIntegrationEvent(): Boolean =
    when (this) {
        is OrderAssignedDomainEvent -> true
        is OrderCompletedDomainEvent -> true
        else -> false
    }

internal fun DomainEvent.toIntegrationEventPayload(): MessageOrBuilder =
    when (this) {
        is OrderAssignedDomainEvent ->
            OrderEventsProto.OrderAssignedIntegrationEvent.newBuilder()
                .setOrderId(orderId.toString())
                .build()

        is OrderCompletedDomainEvent ->
            OrderEventsProto.OrderCompletedIntegrationEvent.newBuilder()
                .setOrderId(orderId.toString())
                .build()

        else -> error("Unsupported domain event: ${this::class.simpleName}")
    }
