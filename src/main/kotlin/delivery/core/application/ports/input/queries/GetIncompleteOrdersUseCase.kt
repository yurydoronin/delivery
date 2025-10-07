package delivery.core.application.ports.input.queries

import arrow.core.Either
import common.types.error.BusinessError
import delivery.core.domain.kernel.Location
import java.util.UUID

interface GetIncompleteOrdersUseCase {
    fun getIncompleteOrders(): Either<BusinessError, List<GetIncompleteOrdersResult>>
}

/**
 * (output DTO) List of incomplete orders
 */
data class GetIncompleteOrdersResult(
    val orderId: UUID,
    val location: Location
)
