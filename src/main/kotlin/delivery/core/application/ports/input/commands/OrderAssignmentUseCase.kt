package delivery.core.application.ports.input.commands

import arrow.core.Either
import common.types.error.BusinessError

interface OrderAssignmentUseCase {
    fun assignTo(): Either<BusinessError, Unit>
}
