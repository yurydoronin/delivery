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

    private var _status: OrderStatus = OrderStatus.CREATED
    val status: OrderStatus
        get() = _status

    private var _courierId: UUID? = null
    val courierId: UUID?
        get() = _courierId

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
            _status = status
            _courierId = courierId
        }
    }

    fun assignToCourier(courierId: UUID) {
        require(_status == OrderStatus.CREATED) { "Only orders in CREATED status can be assigned" }
        _courierId = courierId
        _status = OrderStatus.ASSIGNED

        addDomainEvent(OrderAssignedDomainEvent(orderId = id, courierId))
    }

    fun complete() {
        require(_status == OrderStatus.ASSIGNED) { "Only assigned orders can be completed" }
        checkNotNull(_courierId) { "Cannot complete an order without an assigned courier" }
        _status = OrderStatus.COMPLETED

        addDomainEvent(OrderCompletedDomainEvent(orderId = id, courierId!!))
    }
}

enum class OrderStatus {
    CREATED,
    ASSIGNED,
    COMPLETED
}
