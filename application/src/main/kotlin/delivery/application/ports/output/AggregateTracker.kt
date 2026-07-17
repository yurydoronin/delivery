package delivery.application.ports.output

import delivery.common.types.base.AggregateRoot
import java.util.UUID

/**
 * Трекер регистрирует агрегаты, которые должны участвовать в транзакции
 */
interface AggregateTracker {

    fun track(aggregate: AggregateRoot<UUID>)
    fun getTracked(): List<AggregateRoot<UUID>>
    fun clear()
}
