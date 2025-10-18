package delivery.api.input.adapters.http

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import delivery.core.application.ports.input.commands.OrderCreationCommand
import delivery.core.application.ports.input.commands.OrderCreationUseCase
import delivery.infrastructure.output.adapters.grpc.GeoServiceClientError
import io.mockk.every
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(OrderCreationController::class)
class OrderCreationControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {
    @MockkBean(relaxed = true)
    private lateinit var useCase: OrderCreationUseCase

    @Test
    fun `create order`() {
        // Arrange
        val orderId = UUID.randomUUID()
        val request = OrderCreationRequest(orderId, "Main street", 5)

        every { useCase.execute(request.toCommand()) } returns Either.Right(Unit)

        // Act & Assert
        mockMvc.perform(
            post("/api/v1/orders/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        verify { useCase.execute(OrderCreationCommand(orderId, "Main street", 5)) }
    }

    @Test
    fun `fails to create order`() {
        // Arrange
        val request = OrderCreationRequest(UUID.randomUUID(), "Main street", 5)

        every { useCase.execute(request.toCommand()) } returns Either.Left(GeoServiceClientError.LocationNotFound)

        // Act & Assert
        mockMvc.perform(
            post("/api/v1/orders/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect { content().string("Location not found") }

        verify { useCase.execute(request.toCommand()) }
    }
}