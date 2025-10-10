package delivery.core.application

import delivery.core.application.ports.input.commands.OrderCreationCommand
import delivery.core.application.ports.output.OrderRepositoryPort
import delivery.core.application.ports.output.UnitOfWork
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import org.junit.jupiter.api.Test

class OrderCreationServiceTest {

    val orderRepository: OrderRepositoryPort = mockk(relaxed = true)
    val unitOfWork: UnitOfWork = mockk(relaxed = true)
    val sut = OrderCreationService(orderRepository, unitOfWork)

    @Test
    fun `create order`() {
        // Arrange
        val command = OrderCreationCommand(
            orderID = UUID.randomUUID(),
            "street",
            volume = 10
        )

        // Act
        sut.create(command)

        // Assert
        verify { orderRepository.track(match { it.id == command.orderID && it.volume == command.volume }) }
        verify { unitOfWork.commit() }
    }
}