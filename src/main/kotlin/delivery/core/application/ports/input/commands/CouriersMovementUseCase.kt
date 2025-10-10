package delivery.core.application.ports.input.commands

import arrow.core.Either
import common.types.error.BusinessError

interface CouriersMovementUseCase {
    fun move(): Either<BusinessError, Unit>
}