package delivery.domain.services

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import delivery.common.types.error.BusinessError
import delivery.domain.model.courier.Courier
import delivery.domain.model.order.Order
import delivery.domain.model.order.OrderStatus

/**
 * Система сама распределяет заказы на курьеров.
 * Она берёт любой заказ в статусе Created (не распределённый) и ищет самого подходящего курьера.
 */
fun List<Courier>.dispatch(order: Order): Either<BusinessError, Courier> = either {
    ensure(order.status == OrderStatus.CREATED) {
        DispatchError.InvalidOrderStatusForAssignment
    }

    val winner = filter { it.canTakeOrder(order) }
        .minByOrNull { it.calculateTimeToLocation(order.location) }
        ?: raise(DispatchError.NoAvailableCourier)

    winner.takeOrder(order)
    order.assignToCourierWith(winner.id)

    winner
}

sealed class DispatchError(override val message: String) : BusinessError {
    data object NoAvailableCourier : DispatchError("No available courier can take this order")
    data object InvalidOrderStatusForAssignment : DispatchError("Order must be in CREATED status")
}
