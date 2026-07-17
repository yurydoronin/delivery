package delivery.application.commands

import arrow.core.left
import arrow.core.right
import delivery.application.ports.input.commands.CreateOrderCommand
import delivery.application.ports.input.commands.CreateOrderUseCaseImpl
import delivery.application.ports.output.GeoServiceClientError
import delivery.application.ports.output.GeoServiceClientPort
import delivery.application.ports.output.OrderRepositoryPort
import delivery.application.ports.output.UnitOfWork
import delivery.domain.kernel.Location
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import org.junit.jupiter.api.Test

class CreateOrderUseCaseImplTest {

    val orderRepository: OrderRepositoryPort = mockk(relaxed = true)
    val geoServiceClient: GeoServiceClientPort = mockk(relaxed = true)
    val unitOfWork: UnitOfWork = mockk(relaxed = true)
    val sut = CreateOrderUseCaseImpl(orderRepository, geoServiceClient, unitOfWork)

    @Test
    fun `create order`() {
        // Arrange
        val command = CreateOrderCommand(
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
        val command = CreateOrderCommand(
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