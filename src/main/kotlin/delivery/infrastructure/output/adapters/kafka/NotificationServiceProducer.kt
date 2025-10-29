package delivery.infrastructure.output.adapters.kafka

import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import com.google.protobuf.util.Timestamps.fromMillis
import delivery.core.application.ports.output.MessageBusProducerPort
import delivery.core.domain.model.order.events.OrderCompletedDomainEvent
import delivery.core.domain.model.order.events.OrderCreatedDomainEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import queues.orderstatuschanged.OrderCompletedIntegrationEvent
import queues.orderstatuschanged.OrderCreatedIntegrationEvent

@Service
class NotificationServiceProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @param:Value("app.kafka.orders-events-topic")
    private val topic: String,
) : MessageBusProducerPort {

    private val log = LoggerFactory.getLogger(NotificationServiceProducer::class.java)
    private val printer = JsonFormat.printer().alwaysPrintFieldsWithNoPresence()

    override fun publishOrderCreated(event: OrderCreatedDomainEvent) =
        sendEventToKafka(event.orderId.toString(), event.toIntegrationEvent())

    override fun publishOrderCompleted(event: OrderCompletedDomainEvent) =
        sendEventToKafka(event.orderId.toString(), event.toIntegrationEvent())

    fun sendEventToKafka(key: String, integrationEvent: Message) {
        val json = printer.print(integrationEvent)
        kafkaTemplate.send(topic, key, json)
            .whenComplete { _, ex ->
                ex?.let { log.error("Failed to send event: $key", it) }
                    ?: log.info("Event sent: $key")
            }
    }
}

private fun OrderCreatedDomainEvent.toIntegrationEvent(): OrderCreatedIntegrationEvent =
    OrderCreatedIntegrationEvent.newBuilder()
        .setEventId(eventId.toString())
        .setOrderId(orderId.toString())
        .setOccurredAt(fromMillis(occurredOnUtc.toEpochMilli()))
        .build()

private fun OrderCompletedDomainEvent.toIntegrationEvent(): OrderCompletedIntegrationEvent =
    OrderCompletedIntegrationEvent.newBuilder()
        .setEventId(eventId.toString())
        .setOrderId(orderId.toString())
        .setCourierId(courierId.toString())
        .setOccurredAt(fromMillis(occurredOnUtc.toEpochMilli()))
        .build()
