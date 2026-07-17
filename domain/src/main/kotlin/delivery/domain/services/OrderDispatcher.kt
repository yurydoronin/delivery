package delivery.domain.services

import arrow.core.Either
import delivery.common.types.error.BusinessError
import delivery.domain.model.courier.Courier
import delivery.domain.model.order.Order

/**
 * Система сама распределяет заказы на курьеров.
 * Она берёт любой заказ в статусе Created (не распределённый) и ищет самого подходящего курьера.
 */
interface OrderDispatcher {
    fun dispatch(order: Order, couriers: List<Courier>): Either<BusinessError, Courier>
}