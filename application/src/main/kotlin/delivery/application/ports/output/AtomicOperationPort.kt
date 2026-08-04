package delivery.application.ports.output

import arrow.core.Either

interface AtomicOperationPort {
    fun <E, T> execute(block: () -> Either<E, T>): Either<E, T>
}
