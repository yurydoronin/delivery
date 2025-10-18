package delivery.api.input.adapters.kafka

import com.google.protobuf.util.JsonFormat
import delivery.core.application.ports.input.commands.OrderCreationCommand
import delivery.core.application.ports.input.commands.OrderCreationUseCase
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import queues.basketconfirmed.BasketConfirmedIntegrationEvent

@Service
class BasketConfirmedConsumer(
    private val useCase: OrderCreationUseCase
) {
    private val log = LoggerFactory.getLogger(BasketConfirmedConsumer::class.java)

    @KafkaListener(topics = ["basket.confirmed"], groupId = "basket-group")
    fun listen(message: String) {
        try {
            val builder = BasketConfirmedIntegrationEvent.newBuilder()
            JsonFormat.parser().merge(message, builder)
            val event = builder.build()

            log.info("Received basketId=${event.basketId} with volume=${event.volume}")

            useCase.execute(
                OrderCreationCommand(
                    orderId = UUID.fromString(event.basketId),
                    street = event.address.street,
                    volume = event.volume
                )
            ).fold(
                ifLeft = { error -> log.error("Failed to create order: $error") },
                ifRight = { log.info("Order created successfully for basketId=${event.basketId}") }
            )
        } catch (ex: Exception) {
            log.error(ex.message, ex)
        }
    }
}
