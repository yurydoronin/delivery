package delivery.core.application.ports.input.commands

import arrow.core.Either
import common.types.error.BusinessError

interface OrderAssignmentUseCase {
    fun execute(): Either<BusinessError, Unit>
}
