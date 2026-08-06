package delivery.api.input.adapters.kafka

import com.google.protobuf.util.JsonFormat
import delivery.application.ports.input.commands.CreateOrderCommand
import delivery.application.ports.input.commands.CreateOrderUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import queues.basket.BasketConfirmedIntegrationEvent

@Service
class BasketConfirmedConsumer(
    private val useCase: CreateOrderUseCase
) {
    private val log = KotlinLogging.logger {}

    @KafkaListener(topics = ["baskets.events"], groupId = "basket-group")
    fun listen(message: String) {
        runCatching {
            val builder = BasketConfirmedIntegrationEvent.newBuilder()
            JsonFormat.parser().merge(message, builder)
            val event = builder.build()

            log.info { "Received basketId=${event.basketId} with volume=${event.volume}" }

            useCase.execute(
                CreateOrderCommand(
                    orderId = UUID.fromString(event.basketId),
                    street = event.address.street,
                    volume = event.volume
                )
            ).fold(
                ifLeft = { error ->
                    log.error { "Failed to create order: ${error.message}" }
                },
                ifRight = {
                    log.info { "Order created successfully for basketId=${event.basketId}" }
                }
            )
        }.onFailure { ex ->
            log.error(ex) { ex.message }
        }
    }
}
