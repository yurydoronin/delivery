package delivery.core.application.ports.input.queries

import arrow.core.Either
import common.types.error.BusinessError
import delivery.core.domain.kernel.Location
import java.util.UUID

interface GetAssignedCouriersUseCase {
    fun getAllAssigned(): Either<BusinessError, List<GetAssignedCouriersResult>>
}

/**
 * (output DTO) List of Couriers
 */
data class GetAssignedCouriersResult(
    val courierID: UUID,
    val name: String,
    val location: Location
)
