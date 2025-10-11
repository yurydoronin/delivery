package delivery.api.input.adapters.http

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import delivery.core.application.ActiveOrdersError
import delivery.core.application.ports.input.queries.GetActiveOrdersResult
import delivery.core.application.ports.input.queries.GetActiveOrdersUseCase
import delivery.core.domain.kernel.Location
import io.mockk.every
import java.util.UUID
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(GetActiveOrdersController::class)
class GetActiveOrdersControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {
    @MockkBean
    private lateinit var useCase: GetActiveOrdersUseCase

    @Test
    fun `get active orders`() {
        // Arrange
        val result = GetActiveOrdersResult(UUID.randomUUID(), Location.of(1, 1))

        every { useCase.getActiveOrders() } returns Either.Right(listOf(result))

        val expectedJson = objectMapper.writeValueAsString(
            listOf(
                ActiveOrdersResponse(result.orderId, OrderLocationResponse(1, 1))
            )
        )

        // Act & Assert
        mockMvc.perform(
            get("/api/v1/orders/active")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().json(expectedJson))
    }

    @Test
    fun `fails to get orders`() {
        every { useCase.getActiveOrders() } returns Either.Left(ActiveOrdersError.NoActiveOrders)

        mockMvc.perform(
            get("/api/v1/orders/active")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound)
    }
}
