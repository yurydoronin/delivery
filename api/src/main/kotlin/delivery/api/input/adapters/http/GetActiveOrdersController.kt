package delivery.api.input.adapters.http

import delivery.api.input.adapters.http.dto.OrderResponse
import delivery.api.input.adapters.http.mapper.toResponse
import delivery.application.ports.input.queries.GetActiveOrdersResult
import delivery.application.ports.input.queries.GetActiveOrdersUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/orders/active")
class GetActiveOrdersController(
    private val useCase: GetActiveOrdersUseCase
) {
    @GetMapping
    fun get(): ResponseEntity<List<OrderResponse>> =
        useCase.execute()
            .fold(
                ifLeft = { ResponseEntity.status(HttpStatus.NOT_FOUND).build() },
                ifRight = { results -> ResponseEntity.ok(results.toResponse()) }
            )
}

/**
 * (DTO) HTTP-Response containing the list of active orders
 */
fun List<GetActiveOrdersResult>.toResponse(): List<OrderResponse> =
    map { it.toResponse() }

fun GetActiveOrdersResult.toResponse(): OrderResponse =
    OrderResponse(
        id = orderId,
        location = location.toResponse()
    )
