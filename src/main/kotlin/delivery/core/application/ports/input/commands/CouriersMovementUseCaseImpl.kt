package delivery.core.application.ports.input.commands

import arrow.core.Either
import arrow.core.raise.either
import common.types.error.BusinessError
import delivery.core.application.ports.output.CourierRepositoryPort
import delivery.core.application.ports.output.OrderRepositoryPort
import delivery.core.application.ports.output.UnitOfWork
import delivery.core.domain.model.order.Order
import java.util.UUID
import org.springframework.stereotype.Service

@Service
class CouriersMovementUseCaseImpl(
    private val courierRepository: CourierRepositoryPort,
    private val orderRepository: OrderRepositoryPort,
    private val unitOfWork: UnitOfWork
) : CouriersMovementUseCase {

    override fun execute(): Either<BusinessError, Unit> = either {
        val couriers = courierRepository.getAllCouriers()
            .takeIf { it.isNotEmpty() }
            ?: raise(MovementError.NoCouriers)

        val assignedOrders: Map<UUID, Order> = orderRepository.findAllAssigned()
            .associateBy { it.courierId!! }
            .takeIf { it.isNotEmpty() }
            ?: raise(MovementError.NoOrders)

        couriers.forEach { courier ->
            // берем заказ по курьеру или пропускаем курьера, у которого нет заказов
            val order = assignedOrders[courier.id] ?: return@forEach

            courier.move(order.location)

            if (courier.location == order.location) {
                order.complete()
                courier.completeOrder(order)
            }

            courierRepository.track(courier)
            orderRepository.track(order)
        }

        unitOfWork.commit()
    }
}

sealed class MovementError(override val message: String) : BusinessError {
    data object NoCouriers : MovementError("No couriers found")
    data object NoOrders : MovementError("No assigned orders found")
}
