package delivery.application.ports.input.queries

import arrow.core.Either
import delivery.application.dto.CourierTypeResult
import delivery.application.dto.LocationResult
import delivery.common.types.error.BusinessError
import java.util.UUID

interface GetAllCouriersUseCase {
    fun execute(): Either<BusinessError, List<GetAllCouriersResult>>
}

/**
 * (output DTO) List of couriers
 */
data class GetAllCouriersResult(
    val courierId: UUID,
    val type: CourierTypeResult,
    val location: LocationResult
)
