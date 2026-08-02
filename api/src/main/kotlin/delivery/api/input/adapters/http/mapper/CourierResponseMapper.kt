package delivery.api.input.adapters.http.mapper

import delivery.api.input.adapters.http.dto.CourierTypeResponse
import delivery.api.input.adapters.http.dto.LocationResponse
import delivery.application.dto.CourierTypeResult
import delivery.application.dto.LocationResult

fun CourierTypeResult.toResponse(): CourierTypeResponse =
    when (this) {
        CourierTypeResult.WALKING -> CourierTypeResponse.WALKING
        CourierTypeResult.BICYCLE -> CourierTypeResponse.BICYCLE
        CourierTypeResult.CAR -> CourierTypeResponse.CAR
    }

fun LocationResult.toResponse() =
    LocationResponse(
        x = x,
        y = y
    )
