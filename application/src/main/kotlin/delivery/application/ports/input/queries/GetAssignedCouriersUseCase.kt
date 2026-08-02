package delivery.application.ports.input.queries

import arrow.core.Either
import delivery.application.dto.CourierTypeResult
import delivery.application.dto.LocationResult
import delivery.common.types.error.BusinessError

import java.util.UUID

interface GetAssignedCouriersUseCase {
    fun execute(): Either<BusinessError, List<GetAssignedCouriersResult>>
}

/**
 * (output DTO) List of assigned couriers
 */
data class GetAssignedCouriersResult(
    val courierId: UUID,
    val type: CourierTypeResult,
    val location: LocationResult
)
