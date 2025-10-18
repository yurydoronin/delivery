package delivery.core.application.ports.input.commands

import arrow.core.Either
import arrow.core.raise.either
import common.types.error.BusinessError
import delivery.core.application.ports.output.CourierRepositoryPort
import delivery.core.application.ports.output.OrderRepositoryPort
import delivery.core.application.ports.output.UnitOfWork
import delivery.core.domain.services.OrderDispatcher
import org.springframework.stereotype.Service

@Service
class OrderAssignmentUseCaseImpl(
    private val courierRepository: CourierRepositoryPort,
    private val orderRepository: OrderRepositoryPort,
    private val orderDispatcher: OrderDispatcher,
    private val unitOfWork: UnitOfWork
) : OrderAssignmentUseCase {

    override fun execute(): Either<BusinessError, Unit> = either {
        val order = orderRepository.findAnyCreated()
            ?: raise(OrderAssignmentError.OrderNotFound)

        val couriers = courierRepository.getAvailableCouriers()
        val courier = orderDispatcher.dispatch(order, couriers).bind()

        orderRepository.track(order)
        courierRepository.track(courier)
        unitOfWork.commit()
    }
}

sealed class OrderAssignmentError(override val message: String) : BusinessError {
    data object OrderNotFound : OrderAssignmentError("Any new order not found")
}
