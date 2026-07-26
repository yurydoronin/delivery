package delivery.application.ports.input.commands

import arrow.core.Either
import arrow.core.raise.either
import delivery.application.ports.output.GeoServiceClientPort
import delivery.application.ports.output.OrderRepositoryPort
import delivery.application.ports.output.UnitOfWork
import delivery.common.types.error.BusinessError
import delivery.domain.model.order.Order
import org.springframework.stereotype.Service

@Service
class CreateOrderUseCaseImpl(
    private val orderRepository: OrderRepositoryPort,
    private val geoServiceClient: GeoServiceClientPort,
    private val unitOfWork: UnitOfWork
) : CreateOrderUseCase {

    override fun execute(command: CreateOrderCommand): Either<BusinessError, Unit> = either {
        val location = geoServiceClient.getLocation(command.street).bind()
        val order = Order.of(
            command.orderId,
            location,
            command.volume
        ).bind()

        orderRepository.track(order)
        unitOfWork.commit()
    }
}
