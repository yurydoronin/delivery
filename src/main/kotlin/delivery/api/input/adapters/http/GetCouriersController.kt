package delivery.api.input.adapters.http

import delivery.core.application.ports.input.queries.GetAllCouriersResult
import delivery.core.application.ports.input.queries.GetAllCouriersUseCase
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/couriers")
class GetCouriersController(
    private val useCase: GetAllCouriersUseCase
) {
    @GetMapping
    fun get(): ResponseEntity<List<GetAllCouriersResponse>> =
        useCase.execute()
            .fold(
                ifLeft = { ResponseEntity.status(HttpStatus.NOT_FOUND).build() },
                ifRight = { ResponseEntity.ok(it.toResponse()) }
            )
}

/**
 * (DTO) HTTP-Response containing the list of all couriers
 */
fun List<GetAllCouriersResult>.toResponse(): List<GetAllCouriersResponse> =
    map { it.toResponse() }

fun GetAllCouriersResult.toResponse() =
    GetAllCouriersResponse(
        id = courierId,
        name = name,
        location = LocationResponse(
            x = location.x,
            y = location.y
        )
    )

data class LocationResponse(val x: Int, val y: Int)
data class GetAllCouriersResponse(
    val id: UUID,
    val name: String,
    val location: LocationResponse
)

