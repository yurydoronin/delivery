package delivery.core.application.ports.input.commands

import arrow.core.Either
import delivery.infrastructure.output.adapters.grpc.GeoServiceClientError
import java.util.UUID

interface CreateOrderUseCase {
    fun execute(command: CreateOrderCommand): Either<GeoServiceClientError, Unit>
}

/**
 * (input DTO) Command to create an Order
 */
data class CreateOrderCommand(
    val orderId: UUID,
    val street: String,
    val volume: Int,
)
