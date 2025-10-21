package delivery.core.application

import arrow.core.left
import arrow.core.right
import delivery.core.application.ports.input.commands.OrderCreationCommand
import delivery.core.application.ports.input.commands.OrderCreationUseCaseImpl
import delivery.core.application.ports.output.GeoServiceClientPort
import delivery.core.application.ports.output.OrderRepositoryPort
import delivery.core.application.ports.output.UnitOfWork
import delivery.core.domain.kernel.Location
import delivery.infrastructure.output.adapters.grpc.GeoServiceClientError
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import org.junit.jupiter.api.Test

class OrderCreationUseCaseImplTest {

    val orderRepository: OrderRepositoryPort = mockk(relaxed = true)
    val geoServiceClient: GeoServiceClientPort = mockk(relaxed = true)
    val unitOfWork: UnitOfWork = mockk(relaxed = true)
    val sut = OrderCreationUseCaseImpl(orderRepository, geoServiceClient, unitOfWork)

    @Test
    fun `create order`() {
        // Arrange
        val command = OrderCreationCommand(
            orderId = UUID.randomUUID(),
            street = "Айтишная",
            volume = 10
        )

        every { geoServiceClient.getLocation(command.street) } returns Location.of(1, 1).right()

        // Act
        val result = sut.execute(command)

        // Assert
        result.shouldBeRight()
        verify { orderRepository.track(match { it.id == command.orderId && it.volume == command.volume }) }
        verify { unitOfWork.commit() }
    }

    @Test
    fun `fails to create order`() {
        // Arrange
        val command = OrderCreationCommand(
            orderId = UUID.randomUUID(),
            street = "Айтишная",
            volume = 10
        )

        every { geoServiceClient.getLocation(command.street) } returns GeoServiceClientError.LocationNotFound.left()

        // Act
        val result = sut.execute(command)

        // Assert
        result shouldBe GeoServiceClientError.LocationNotFound.left()
    }
}