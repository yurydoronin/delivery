package delivery.api.input.adapters.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import delivery.core.application.ports.input.commands.OrderCreationCommand
import delivery.core.application.ports.input.commands.OrderCreationUseCase
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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

        // Act & Assert
        mockMvc.perform(
            post("/api/v1/orders/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        verify { useCase.create(OrderCreationCommand(orderId, "Main street", 5)) }
    }
}