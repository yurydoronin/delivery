package delivery.api.input.adapters.http

import com.fasterxml.jackson.annotation.JsonProperty
import delivery.application.ports.input.commands.CreateOrderCommand
import delivery.application.ports.input.commands.CreateOrderUseCase
import delivery.domain.model.order.Address
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/orders")
class CreateOrderController(
    private val useCase: CreateOrderUseCase
) {
    @PostMapping
    fun create(@RequestBody request: OrderCreationRequest): ResponseEntity<String> =
        useCase.execute(request.toCommand()).fold(
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
    @JsonProperty("id")
    val orderId: UUID,
    val address: Address,
    val volume: Int,
)

fun OrderCreationRequest.toCommand(): CreateOrderCommand =
    CreateOrderCommand(
        orderId = orderId,
        street = address.street,
        volume = volume,
    )