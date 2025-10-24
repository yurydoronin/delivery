package delivery.core.domain.model.order

import common.types.base.Aggregate
import delivery.core.domain.kernel.Location
import delivery.core.domain.model.order.events.OrderCompletedDomainEvent
import delivery.core.domain.model.order.events.OrderCreatedDomainEvent
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "orders")
class Order private constructor(
    id: UUID,
    @Embedded
    val location: Location,
    val volume: Int
) : Aggregate<UUID>(id) {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private var _status: OrderStatus = OrderStatus.CREATED
    val status: OrderStatus
        get() = _status

    @Column(name = "courier_id")
    private var _courierId: UUID? = null
    val courierId: UUID?
        get() = _courierId

    companion object {
        fun of(id: UUID, location: Location, volume: Int): Order {
            require(volume > 0) { "Volume must be positive" }
            val order = Order(id, location, volume)
            order.addDomainEvent(OrderCreatedDomainEvent(order))
            return order
        }
    }

    fun assignToCourier(courierId: UUID) {
        require(_status == OrderStatus.CREATED) { "Only orders in CREATED status can be assigned" }
        _courierId = courierId
        _status = OrderStatus.ASSIGNED
    }

    fun complete() {
        require(_status == OrderStatus.ASSIGNED) { "Only assigned orders can be completed" }
        checkNotNull(_courierId) { "Cannot complete an order without an assigned courier" }
        _status = OrderStatus.COMPLETED

        addDomainEvent(OrderCompletedDomainEvent(this))
    }
}

enum class OrderStatus {
    CREATED,
    ASSIGNED,
    COMPLETED
}
