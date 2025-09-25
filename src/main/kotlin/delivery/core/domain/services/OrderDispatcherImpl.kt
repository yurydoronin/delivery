package delivery.core.domain.services

import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.courier.StorageCheck
import delivery.core.domain.model.order.Order
import delivery.core.domain.model.order.OrderStatus
import org.springframework.stereotype.Service

@Service
class OrderDispatcherImpl : OrderDispatcher {

    override fun dispatch(order: Order, couriers: List<Courier>): Courier {
        require(order.status == OrderStatus.CREATED) { "Order must be in CREATED status" }

        val winner = couriers
            .filter { it.storagePlaces.any { sp -> sp.canStore(order.volume) is StorageCheck.Ok } }
            .minByOrNull { it.calculateTimeToLocation(order.location) }
            ?: throw IllegalArgumentException("No available courier can take this order")

        winner.takeOrder(order)
        order.assignToCourier(winner.id)

        return winner
    }

}