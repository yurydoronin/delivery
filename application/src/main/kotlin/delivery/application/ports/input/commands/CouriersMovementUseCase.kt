package delivery.application.ports.input.commands

import arrow.core.Either
import delivery.common.types.error.BusinessError

interface CouriersMovementUseCase {
    fun execute(): Either<BusinessError, Unit>
}