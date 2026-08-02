package delivery.api.input.adapters.http

import delivery.api.input.adapters.http.dto.CourierResponse
import delivery.api.input.adapters.http.mapper.toResponse
import delivery.application.ports.input.queries.GetAssignedCouriersResult
import delivery.application.ports.input.queries.GetAssignedCouriersUseCase
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
    fun get(): ResponseEntity<List<CourierResponse>> =
        useCase.execute()
            .fold(
                ifLeft = {
                    ResponseEntity.status(HttpStatus.NOT_FOUND).build()
                },
                ifRight = {
                    ResponseEntity.ok(it.toResponse())
                }
            )
}

fun List<GetAssignedCouriersResult>.toResponse(): List<CourierResponse> =
    map { it.toResponse() }

fun GetAssignedCouriersResult.toResponse() =
    CourierResponse(
        id = courierId,
        type = type.toResponse(),
        location = location.toResponse()
    )
