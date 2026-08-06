package delivery.infrastructure.output.adapters

import arrow.core.Either
import delivery.application.ports.output.AtomicOperationPort
import io.github.oshai.kotlinlogging.KotlinLogging
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

    private val log = KotlinLogging.logger {}

    override fun <E, T> execute(block: () -> Either<E, T>): Either<E, T> =
        transactionTemplate.execute { status ->
            block().onLeft { error ->
                log.warn { "Atomic operation rolled back. Reason: $error" }
                status.setRollbackOnly()
            }
        }
}
