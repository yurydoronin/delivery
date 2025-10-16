package delivery.api.input.adapters.http

import delivery.core.application.ports.input.commands.OrderCreationCommand
import delivery.core.application.ports.input.commands.OrderCreationUseCase
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/orders/create")
class OrderCreationController(
    private val useCase: OrderCreationUseCase
) {
    @PostMapping
    fun create(@RequestBody request: OrderCreationRequest): ResponseEntity<String> =
        useCase.create(request.toCommand()).fold(
            ifLeft = { error ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.message)
            },
            ifRight = {
                ResponseEntity.status(HttpStatus.CREATED).build()
            }
        )
}

/**
 * (DTO) HTTP-Request to create an order
 */
data class OrderCreationRequest(
    val orderId: UUID,
    val street: String,
    val volume: Int,
)

fun OrderCreationRequest.toCommand(): OrderCreationCommand =
    OrderCreationCommand(
        orderId = orderId,
        street = street,
        volume = volume,
    )