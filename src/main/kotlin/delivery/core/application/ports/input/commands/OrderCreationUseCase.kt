package delivery.core.application.ports.input.commands

import java.util.UUID

interface OrderCreationUseCase {
    fun create(command: OrderCreationCommand)
}

/**
 * (input DTO) Command to create an Order
 */
data class OrderCreationCommand(
    val orderID: UUID,
    val street: String,
    val volume: Int,
)