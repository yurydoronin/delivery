package delivery.core.application.ports.input.commands

import arrow.core.Either
import arrow.core.raise.either
import delivery.core.application.ports.output.GeoServiceClientPort
import delivery.core.application.ports.output.OrderRepositoryPort
import delivery.core.application.ports.output.UnitOfWork
import delivery.core.domain.model.order.Order
import delivery.infrastructure.output.adapters.grpc.GeoServiceClientError
import org.springframework.stereotype.Service

@Service
class OrderCreationUseCaseImpl(
    private val orderRepository: OrderRepositoryPort,
    private val geoServiceClient: GeoServiceClientPort,
    private val unitOfWork: UnitOfWork
) : OrderCreationUseCase {

    override fun execute(command: OrderCreationCommand): Either<GeoServiceClientError, Unit> = either {
        val location = geoServiceClient.getLocation(command.street).bind()
        val order = Order.of(
            command.orderId,
            location,
            command.volume
        )

        orderRepository.track(order)
        unitOfWork.commit()
    }
}