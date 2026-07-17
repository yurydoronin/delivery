package delivery.application.ports.input.commands

import arrow.core.Either
import arrow.core.raise.either
import delivery.application.ports.output.CourierRepositoryPort
import delivery.application.ports.output.OrderRepositoryPort
import delivery.application.ports.output.UnitOfWork
import delivery.common.types.error.BusinessError
import delivery.domain.services.OrderDispatcher
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AssignOrderUseCaseImpl(
    private val courierRepository: CourierRepositoryPort,
    private val orderRepository: OrderRepositoryPort,
    private val orderDispatcher: OrderDispatcher,
    private val unitOfWork: UnitOfWork
) : AssignOrderUseCase {

    @Transactional
    override fun execute(orderId: UUID): Either<BusinessError, Unit> = either {
        val order = orderRepository.findById(orderId)
            ?: raise(OrderAssignmentError.OrderNotFound)

        val couriers = courierRepository.findCouriersWithAnyFreeStorage()
        val courier = orderDispatcher.dispatch(order, couriers).bind()

        orderRepository.track(order)
        courierRepository.track(courier)
        unitOfWork.commit()
    }
}

sealed class OrderAssignmentError(override val message: String) : BusinessError {
    data object OrderNotFound : OrderAssignmentError("Any new order not found")
}
