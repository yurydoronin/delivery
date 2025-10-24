package delivery.core.application.ports.input.queries

import arrow.core.Either
import common.types.error.BusinessError
import delivery.core.domain.kernel.Location
import java.util.UUID

interface GetAssignedCouriersUseCase {
    fun execute(): Either<BusinessError, List<GetAssignedCouriersResult>>
}

/**
 * (output DTO) List of assigned couriers
 */
data class GetAssignedCouriersResult(
    val courierId: UUID,
    val name: String,
    val location: Location
)
