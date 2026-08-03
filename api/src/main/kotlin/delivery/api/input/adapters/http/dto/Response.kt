package delivery.api.input.adapters.http.dto
import java.util.UUID

data class CourierResponse(
    val id: UUID,
    val type: CourierTypeResponse,
    val location: LocationResponse
)

enum class CourierTypeResponse {
    WALKING,
    BICYCLE,
    CAR
}

data class OrderResponse(
    val id: UUID,
    val location: LocationResponse
)

data class LocationResponse(
    val x: Int,
    val y: Int
)
