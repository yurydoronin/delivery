package delivery.api.input.adapters.http

import delivery.core.application.ports.input.queries.GetActiveOrdersResult
import delivery.core.application.ports.input.queries.GetActiveOrdersUseCase
import java.util.UUID
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
    fun get(): ResponseEntity<List<ActiveOrdersResponse>> =
        useCase.execute()
            .fold(
                ifLeft = { ResponseEntity.status(HttpStatus.NOT_FOUND).build() },
                ifRight = { results -> ResponseEntity.ok(results.toResponse()) }
            )
}

/**
 * (DTO) HTTP-Response containing the list of active orders
 */
fun List<GetActiveOrdersResult>.toResponse(): List<ActiveOrdersResponse> =
    map { it.toResponse() }

fun GetActiveOrdersResult.toResponse(): ActiveOrdersResponse =
    ActiveOrdersResponse(
        id = orderId,
        location = OrderLocationResponse(
            x = location.x,
            y = location.y
        )
    )

data class OrderLocationResponse(val x: Int, val y: Int)
data class ActiveOrdersResponse(
    val id: UUID,
    val location: OrderLocationResponse
)
