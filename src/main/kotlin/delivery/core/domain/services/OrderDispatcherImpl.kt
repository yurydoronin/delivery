package delivery.core.domain.services

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import common.types.error.BusinessError
import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.order.Order
import delivery.core.domain.model.order.OrderStatus
import org.springframework.stereotype.Service

@Service
class OrderDispatcherImpl : OrderDispatcher {

    override fun dispatch(order: Order, couriers: List<Courier>): Either<DispatchError, Courier> = either {
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