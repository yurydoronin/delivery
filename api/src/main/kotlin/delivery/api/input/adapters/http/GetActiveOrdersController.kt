package delivery.api.input.adapters.http

import delivery.api.input.adapters.http.dto.OrdersResponse
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
    fun get(): ResponseEntity<List<OrdersResponse>> =
        useCase.execute()
            .fold(
                ifLeft = { ResponseEntity.status(HttpStatus.NOT_FOUND).build() },
                ifRight = { results -> ResponseEntity.ok(results.toResponse()) }
            )
}

/**
 * (DTO) HTTP-Response containing the list of active orders
 */
fun List<GetActiveOrdersResult>.toResponse(): List<OrdersResponse> =
    map { it.toResponse() }

fun GetActiveOrdersResult.toResponse(): OrdersResponse =
    OrdersResponse(
        id = orderId,
        location = location.toResponse()
    )
