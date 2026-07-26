package delivery.application.ports.input.commands

import arrow.core.Either
import delivery.common.types.error.BusinessError
import java.util.UUID

interface CreateOrderUseCase {
    fun execute(command: CreateOrderCommand): Either<BusinessError, Unit>
}

/**
 * (input DTO) Command to create an Order
 */
data class CreateOrderCommand(
    val orderId: UUID,
    val street: String,
    val volume: Int,
)
