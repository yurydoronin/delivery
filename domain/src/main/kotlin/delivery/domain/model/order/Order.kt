package delivery.domain.model.order

import delivery.common.types.base.Aggregate
import delivery.domain.kernel.Location
import delivery.domain.model.order.events.OrderAssignedDomainEvent
import delivery.domain.model.order.events.OrderCompletedDomainEvent
import delivery.domain.model.order.events.OrderCreatedDomainEvent
import java.util.UUID

/**
 * Заказ
 */
class Order private constructor(
    id: UUID,
    val location: Location,
    val volume: Int
) : Aggregate<UUID>(id) {

    var status: OrderStatus = OrderStatus.CREATED
        private set

    var courierId: UUID? = null
        private set

    companion object {
        fun of(
            id: UUID,
            location: Location,
            volume: Int,
        ): Order {
            require(volume > 0) { "Volume must be positive" }

            return Order(
                id = id,
                location = location,
                volume = volume,
            )
                .apply {
                    addDomainEvent(OrderCreatedDomainEvent(orderId = id))
                }
        }

        fun restore(
            id: UUID,
            location: Location,
            volume: Int,
            status: OrderStatus,
            courierId: UUID?,
        ) = Order(
            id = id,
            location = location,
            volume = volume,
        ).apply {
            this.status = status
            this.courierId = courierId
        }
    }

    fun assignToCourier(courierId: UUID) {
        require(status == OrderStatus.CREATED) { "Only orders in CREATED status can be assigned" }
        this.courierId = courierId
        this.status = OrderStatus.ASSIGNED

        addDomainEvent(OrderAssignedDomainEvent(orderId = id, courierId))
    }

    fun complete() {
        require(status == OrderStatus.ASSIGNED) { "Only assigned orders can be completed" }
        checkNotNull(courierId) { "Cannot complete an order without an assigned courier" }
        this.status = OrderStatus.COMPLETED

        addDomainEvent(OrderCompletedDomainEvent(orderId = id, courierId!!))
    }
}

enum class OrderStatus {
    CREATED,
    ASSIGNED,
    COMPLETED
}
