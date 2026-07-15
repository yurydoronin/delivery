package delivery.core.application.ports.input.commands

import arrow.core.Either
import common.types.error.BusinessError
import java.util.UUID

interface AssignOrderUseCase {
    fun execute(orderId: UUID): Either<BusinessError, Unit>
}
