package delivery.domain.services

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import delivery.common.types.error.BusinessError
import delivery.domain.model.courier.Courier
import delivery.domain.model.order.Order
import delivery.domain.model.order.OrderStatus

class OrderDispatcherImpl : OrderDispatcher {

    override fun dispatch(order: Order, couriers: List<Courier>): Either<BusinessError, Courier> = either {
        ensure(order.status == OrderStatus.CREATED) {
            DispatchError.ValidationError
        }

        val winner = couriers
            .filter { it.canTakeOrder(order) }
            .minByOrNull { it.calculateTimeToLocation(order.location) }
            ?: raise(DispatchError.NoAvailableCourier)

        winner.takeOrder(order)
        order.assignToCourier(winner.id)

        winner
    }
}

sealed class DispatchError(override val message: String) : BusinessError {
    data object NoAvailableCourier : DispatchError("No available courier can take this order")
    data object ValidationError : DispatchError("Order must be in CREATED status")
}
