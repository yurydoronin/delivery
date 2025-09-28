package delivery.core.domain.services

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import common.types.error.BusinessError
import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.courier.StorageCheck
import delivery.core.domain.model.order.Order
import delivery.core.domain.model.order.OrderStatus
import org.springframework.stereotype.Service

@Service
class OrderDispatcherImpl : OrderDispatcher {

    override fun dispatch(order: Order, couriers: List<Courier>): Either<DispatchError, Courier> {
        require(order.status == OrderStatus.CREATED) { "Order must be in CREATED status" }

        val winner = couriers
            .filter { it.storagePlaces.any { sp -> sp.canStore(order.volume) == StorageCheck.Ok } }
            .minByOrNull { it.calculateTimeToLocation(order.location) }
            ?: return DispatchError.NoAvailableCourier.left()

        winner.takeOrder(order)
        order.assignToCourier(winner.id)

        return winner.right()
    }
}

sealed class DispatchError(override val message: String) : BusinessError {
    data object NoAvailableCourier : DispatchError("No available courier can take this order")
}