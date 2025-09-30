package delivery.core.application.ports.output

import common.types.base.AggregateRoot

/**
 * Трекер регистрирует аггрегаты, которые должны участвовать в транзакции
 */
interface AggregateTracker {

    fun track(aggregate: AggregateRoot)
    fun getTracked(): List<AggregateRoot>
    fun clear()
}