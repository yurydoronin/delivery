package delivery.core.application.ports.input.queries

import arrow.core.Either
import common.types.error.BusinessError
import delivery.core.domain.kernel.Location
import java.util.UUID

interface GetActiveOrdersUseCase {
    fun execute(): Either<BusinessError, List<GetActiveOrdersResult>>
}

/**
 * (output DTO) List of active orders
 */
data class GetActiveOrdersResult(
    val orderId: UUID,
    val location: Location
)
