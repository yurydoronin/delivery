package delivery.core.application.ports.input.commands

import arrow.core.Either
import delivery.infrastructure.output.adapters.grpc.GeoServiceClientError
import java.util.UUID

interface OrderCreationUseCase {
    fun execute(command: OrderCreationCommand): Either<GeoServiceClientError, Unit>
}

/**
 * (input DTO) Command to create an Order
 */
data class OrderCreationCommand(
    val orderId: UUID,
    val street: String,
    val volume: Int,
)