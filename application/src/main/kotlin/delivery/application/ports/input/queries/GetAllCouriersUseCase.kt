package delivery.application.ports.input.queries

import arrow.core.Either
import delivery.common.types.dto.LocationResult
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
    val name: String,
    val location: LocationResult
)
