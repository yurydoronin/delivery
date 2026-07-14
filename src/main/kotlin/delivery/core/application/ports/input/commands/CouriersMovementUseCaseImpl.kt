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
import org.springframework.transaction.annotation.Transactional

@Service
class CouriersMovementUseCaseImpl(
    private val courierRepository: CourierRepositoryPort,
    private val orderRepository: OrderRepositoryPort,
    private val unitOfWork: UnitOfWork
) : CouriersMovementUseCase {

    @Transactional
    override fun execute(): Either<BusinessError, Unit> = either {
        // получаем только курьеров с заказами
        val couriers = courierRepository.getCouriersWithAssignedOrders()
            .takeIf { it.isNotEmpty() }
            ?: raise(MovementError.NoCouriers)

        // получаем заказы, назначенные на курьеров
        val assignedOrders: Map<UUID, Order> = orderRepository.findAllAssigned()
            .associateBy { it.courierId!! }
            .takeIf { it.isNotEmpty() }
            ?: raise(MovementError.NoOrders)

        couriers.forEach { courier ->
            val order = assignedOrders[courier.id] // берем заказ по курьеру

            courier.move(order!!.location)

            if (courier.location == order.location) {
                order.complete()
                courier.delivered(order)
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
