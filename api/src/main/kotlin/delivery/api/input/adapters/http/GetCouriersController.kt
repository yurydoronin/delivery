package delivery.api.input.adapters.http

import delivery.api.input.adapters.http.dto.CourierResponse
import delivery.api.input.adapters.http.mapper.toResponse
import delivery.application.ports.input.queries.GetAllCouriersResult
import delivery.application.ports.input.queries.GetAllCouriersUseCase
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
    fun get(): ResponseEntity<List<CourierResponse>> =
        useCase.execute()
            .fold(
                ifLeft = { ResponseEntity.status(HttpStatus.NOT_FOUND).build() },
                ifRight = { ResponseEntity.ok(it.toResponse()) }
            )
}

/**
 * (DTO) HTTP-Response containing the list of all couriers
 */
fun List<GetAllCouriersResult>.toResponse(): List<CourierResponse> =
    map { it.toResponse() }

fun GetAllCouriersResult.toResponse() =
    CourierResponse(
        id = courierId,
        type = type.toResponse(),
        location = location.toResponse()
    )
