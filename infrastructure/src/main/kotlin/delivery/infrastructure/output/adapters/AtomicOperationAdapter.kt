package delivery.infrastructure.output.adapters

import arrow.core.Either
import delivery.application.ports.output.AtomicOperationPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

/**
 * Spring-реализация [AtomicOperationPort].
 *
 * Использует [TransactionTemplate] для выполнения каждой операции в отдельной
 * транзакции (`PROPAGATION_REQUIRES_NEW`). При получении [Either.Left]
 * помечает транзакцию на откат без выбрасывания исключения, сохраняя
 * типизированную бизнес-ошибку.
 */
@Component
class AtomicOperationAdapter(
    transactionTemplate: TransactionTemplate
) : AtomicOperationPort {

    private val transactionTemplate = transactionTemplate.apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
    }

    private val log = LoggerFactory.getLogger(javaClass)

    override fun <E, T> execute(block: () -> Either<E, T>): Either<E, T> =
        transactionTemplate.execute { status ->
            when (val result = block()) {
                is Either.Right -> result
                is Either.Left -> {

                    log.warn("Atomic operation rolled back. Reason: ${result.value}")

                    status.setRollbackOnly()
                    result
                }
            }
        }
}
