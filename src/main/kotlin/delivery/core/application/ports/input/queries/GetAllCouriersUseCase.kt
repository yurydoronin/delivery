package delivery.core.application.ports.input.queries

import arrow.core.Either
import common.types.error.BusinessError
import delivery.core.domain.kernel.Location
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
    val location: Location
)
