package delivery.infrastructure.output.adapters

import arrow.core.Either
import delivery.application.ports.output.atomic.AtomicOperationPort
import delivery.application.ports.output.atomic.TransactionPropagation
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

/**
 * Spring-реализация [AtomicOperationPort].
 *
 * Использует [TransactionTemplate] для программного управления транзакцией.
 * Способ распространения транзакции определяется параметром [TransactionPropagation].
 *
 * Для каждого варианта распространения используется отдельный заранее
 * сконфигурированный [TransactionTemplate]. Шаблоны не изменяются после
 * инициализации компонента и поэтому могут безопасно использоваться
 * конкурентными потоками.
 *
 * Не используется один общий [TransactionTemplate] с изменением
 * `propagationBehavior` перед каждым вызовом, поскольку это создало бы
 * общее изменяемое состояние и требовало бы синхронизации доступа.
 *
 * Если операция возвращает [Either.Left], текущая транзакция помечается
 * на откат (`rollback`) без выбрасывания исключения, сохраняя типизированную
 * бизнес-ошибку.
 *
 * Если операция возвращает [Either.Right], транзакция фиксируется (`commit`).
 */
@Component
class AtomicOperationAdapter(
    transactionManager: PlatformTransactionManager,
) : AtomicOperationPort {

    private val log = KotlinLogging.logger {}

    private val requiredTemplate = TransactionTemplate(transactionManager).apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
    }
    private val requiresNewTemplate = TransactionTemplate(transactionManager).apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
    }

    override fun <E, T> execute(
        propagation: TransactionPropagation,
        block: () -> Either<E, T>,
    ): Either<E, T> {
        val transactionTemplate = when (propagation) {
            TransactionPropagation.REQUIRED -> requiredTemplate
            TransactionPropagation.REQUIRES_NEW -> requiresNewTemplate
        }

        return checkNotNull(
            transactionTemplate.execute { status ->
                block().onLeft { error ->
                    log.warn { "Atomic operation rolled back. Reason: $error" }
                    status.setRollbackOnly()
                }
            }
        ) { "TransactionTemplate returned null" }
    }
}
