package delivery.core.application.ports.output

import common.types.base.AggregateRoot
import java.util.UUID

/**
 * Трекер регистрирует аггрегаты, которые должны участвовать в транзакции
 */
interface AggregateTracker {

    fun track(aggregate: AggregateRoot<UUID>)
    fun getTracked(): List<AggregateRoot<UUID>>
    fun clear()
}