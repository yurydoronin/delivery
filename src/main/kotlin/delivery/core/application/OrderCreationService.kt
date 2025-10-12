package delivery.core.application

import delivery.core.application.ports.input.commands.OrderCreationCommand
import delivery.core.application.ports.input.commands.OrderCreationUseCase
import delivery.core.application.ports.output.OrderRepositoryPort
import delivery.core.application.ports.output.UnitOfWork
import delivery.core.domain.kernel.Location
import delivery.core.domain.model.order.Order
import org.springframework.stereotype.Service

@Service
class OrderCreationService(
    private val orderRepository: OrderRepositoryPort,
    private val unitOfWork: UnitOfWork
) : OrderCreationUseCase {

    override fun create(command: OrderCreationCommand) {
        orderRepository.track(
            Order.of(
                command.orderID,
                Location.random(),
                command.volume
            )
        )
        unitOfWork.commit()
    }
}