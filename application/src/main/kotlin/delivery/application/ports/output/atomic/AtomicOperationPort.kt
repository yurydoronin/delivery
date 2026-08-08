package delivery.application.ports.output.atomic

import arrow.core.Either

/**
 * Выполняет бизнес-операцию атомарно в рамках транзакции.
 *
 * Позволяет приложению управлять границей транзакции независимо от
 * конкретного механизма транзакционного управления инфраструктуры.
 *
 * [Either.Right] означает успешное выполнение операции и позволяет
 * зафиксировать транзакцию (`commit`).
 *
 * [Either.Left] означает бизнес-ошибку. В этом случае адаптер помечает
 * текущую транзакцию на откат (`rollback`) и возвращает ошибку вызывающему
 * коду без преобразования её в исключение.
 *
 * [TransactionPropagation] определяет, как операция взаимодействует
 * с существующей транзакцией.
 */
interface AtomicOperationPort {
    /**
     * Выполняет операцию в атомарной транзакции.
     *
     * - [Either.Right] — транзакция фиксируется (`commit`).
     * - [Either.Left] — транзакция откатывается (`rollback`), а ошибка
     *   возвращается вызывающему коду.
     */
    fun <E, T> execute(
        propagation: TransactionPropagation = TransactionPropagation.REQUIRED,
        block: () -> Either<E, T>,
    ): Either<E, T>
}
