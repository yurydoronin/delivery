package delivery.domain.model.order

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import delivery.common.types.base.Aggregate
import delivery.common.types.error.BusinessError
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
        ): Either<OrderError, Order> = either {

            ensure(volume > 0) { OrderError.InvalidVolume }

            Order(
                id = id,
                location = location,
                volume = volume,
            )
                .apply {
                    addDomainEvent(
                        OrderCreatedDomainEvent(orderId = id)
                    )
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

    fun assignToCourier(courierId: UUID): Either<OrderError, Unit> = either {
        ensure(status == OrderStatus.CREATED) { OrderError.InvalidStatusForAssignment }

        this@Order.courierId = courierId
        status = OrderStatus.ASSIGNED

        addDomainEvent(
            OrderAssignedDomainEvent(orderId = id, courierId)
        )
    }

    fun complete(): Either<OrderError, Unit> = either {
        ensure(status == OrderStatus.ASSIGNED) { OrderError.InvalidStatusForCompletion }
        val courierId = courierId
            ?: raise(OrderError.CourierNotAssigned)

        status = OrderStatus.COMPLETED

        addDomainEvent(OrderCompletedDomainEvent(orderId = id, courierId))
    }
}

enum class OrderStatus {
    CREATED,
    ASSIGNED,
    COMPLETED
}

sealed class OrderError(override val message: String) : BusinessError {
    data object InvalidVolume : OrderError("Volume must be positive")
    data object InvalidStatusForAssignment : OrderError("Only orders in CREATED status can be assigned")
    data object InvalidStatusForCompletion : OrderError("Only assigned orders can be completed")
    data object CourierNotAssigned : OrderError("Cannot complete an order without an assigned courier")
}
