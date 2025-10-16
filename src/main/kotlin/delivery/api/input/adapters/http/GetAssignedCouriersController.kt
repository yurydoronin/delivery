package delivery.api.input.adapters.http

import delivery.core.application.ports.input.queries.GetAssignedCouriersResult
import delivery.core.application.ports.input.queries.GetAssignedCouriersUseCase
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/couriers/assigned")
class GetAssignedCouriersController(
    private val useCase: GetAssignedCouriersUseCase
) {
    @GetMapping
    fun get(): ResponseEntity<List<GetAssignedCouriersResponse>> =
        useCase.getAllAssigned()
            .fold(
                ifLeft = { ResponseEntity.status(HttpStatus.NOT_FOUND).build() },
                ifRight = { ResponseEntity.ok(it.toResponse()) }
            )
}

/**
 * (DTO) HTTP-Response containing the list of assigned couriers
 */
fun List<GetAssignedCouriersResult>.toResponse(): List<GetAssignedCouriersResponse> =
    map { it.toResponse() }

fun GetAssignedCouriersResult.toResponse() =
    GetAssignedCouriersResponse(
        id = courierId,
        name = name,
        location = CourierLocationResponse(
            x = location.x,
            y = location.y
        )
    )

data class CourierLocationResponse(val x: Int, val y: Int)
data class GetAssignedCouriersResponse(
    val id: UUID,
    val name: String,
    val location: CourierLocationResponse
)

