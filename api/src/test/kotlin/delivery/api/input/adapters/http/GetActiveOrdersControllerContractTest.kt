package delivery.api.input.adapters.http

import arrow.core.Either
import com.ninjasquad.springmockk.MockkBean
import delivery.application.ports.input.queries.ActiveOrdersError
import delivery.application.ports.input.queries.GetActiveOrdersResult
import delivery.application.ports.input.queries.GetActiveOrdersUseCase
import delivery.domain.kernel.Location
import io.mockk.every
import java.util.UUID
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

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

        every { useCase.execute() } returns Either.Right(listOf(result))

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
        every { useCase.execute() } returns Either.Left(ActiveOrdersError.NoActiveOrders)

        mockMvc.perform(
            get("/api/v1/orders/active")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound)
    }
}
