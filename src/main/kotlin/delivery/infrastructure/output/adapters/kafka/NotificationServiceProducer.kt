package delivery.infrastructure.output.adapters.kafka

import com.google.protobuf.util.JsonFormat
import com.google.protobuf.util.Timestamps.fromMillis
import common.types.base.DomainEvent
import delivery.core.application.ports.output.MessageBusProducerPort
import delivery.core.domain.model.order.events.OrderCompletedDomainEvent
import delivery.core.domain.model.order.events.OrderCreatedDomainEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import queues.orderstatuschanged.OrderCompletedIntegrationEvent
import queues.orderstatuschanged.OrderCreatedIntegrationEvent

@Service
class NotificationServiceProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @param:Value("app.kafka.order-status-changed-topic")
    private val topic: String
) : MessageBusProducerPort {

    override fun <E : DomainEvent> publish(event: E) {
        when (event) {
            is OrderCreatedDomainEvent -> {
                val integrationEventAsJson = JsonFormat.printer().alwaysPrintFieldsWithNoPresence()
                    .print(event.toIntegrationEvent())

                kafkaTemplate.send(topic, event.orderId.toString(), integrationEventAsJson)
            }

            is OrderCompletedDomainEvent -> {
                val integrationEventAsJson = JsonFormat.printer().alwaysPrintFieldsWithNoPresence()
                    .print(event.toIntegrationEvent())

                kafkaTemplate.send(topic, event.orderId.toString(), integrationEventAsJson)
            }

            else -> {
                "Unsupported event type: ${event::class.simpleName}"
            }
        }
    }
}

fun OrderCreatedDomainEvent.toIntegrationEvent(): OrderCreatedIntegrationEvent =
    OrderCreatedIntegrationEvent.newBuilder()
        .setEventId(eventId.toString())
        .setOrderId(orderId.toString())
        .setOccurredAt(fromMillis(occurredOnUtc.toEpochMilli()))
        .build()

fun OrderCompletedDomainEvent.toIntegrationEvent(): OrderCompletedIntegrationEvent =
    OrderCompletedIntegrationEvent.newBuilder()
        .setEventId(eventId.toString())
        .setOrderId(orderId.toString())
        .setCourierId(courierId.toString())
        .setOccurredAt(fromMillis(occurredOnUtc.toEpochMilli()))
        .build()
