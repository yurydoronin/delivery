package delivery.core.domain.services

import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.order.Order

/**
 * Система сама распределяет заказы на курьеров.
 * Она берёт любой заказ в статусе Created (не распределённый) и ищет самого подходящего курьера.
 */
interface OrderDispatcher {

    fun dispatch(order: Order, couriers: List<Courier>): Courier
}