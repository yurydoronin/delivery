package delivery.api

import arrow.core.Either
import com.ninjasquad.springmockk.MockkBean
import delivery.ApplicationTestConfig
import delivery.api.input.adapters.http.CreateOrderController
import delivery.api.input.adapters.http.OrderCreationRequest
import delivery.api.input.adapters.http.toCommand
import delivery.application.ports.input.commands.CreateOrderCommand
import delivery.application.ports.input.commands.CreateOrderUseCase
import delivery.application.ports.output.GeoServiceClientError
import delivery.domain.model.order.Address
import io.mockk.every
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@WebMvcTest(CreateOrderController::class, ApplicationTestConfig::class)
class CreateOrderControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {
    @MockkBean(relaxed = true)
    private lateinit var useCase: CreateOrderUseCase

    @Test
    fun `create order`() {
        // Arrange
        val orderId = UUID.randomUUID()
        val address = Address.of("Россия", "Москва", "Ленина", 1, 10)
        val request = OrderCreationRequest(orderId, address, 5)

        every { useCase.execute(request.toCommand()) } returns Either.Right(Unit)

        // Act & Assert
        mockMvc.perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        verify { useCase.execute(CreateOrderCommand(orderId, "Ленина", 5)) }
    }

    @Test
    fun `fails to create order`() {
        // Arrange
        val address = Address.of("Россия", "Москва", "Ленина", 1, 10)
        val request = OrderCreationRequest(UUID.randomUUID(), address, 5)

        every { useCase.execute(request.toCommand()) } returns Either.Left(GeoServiceClientError.LocationNotFound)

        // Act & Assert
        mockMvc.perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect { content().string("Location not found") }

        verify { useCase.execute(request.toCommand()) }
    }
}