package delivery.api.input.adapters.http

import arrow.core.Either
import com.ninjasquad.springmockk.MockkBean
import delivery.core.application.MovementError
import delivery.core.application.OrderAssignmentError
import delivery.core.application.ports.input.commands.CouriersMovementUseCase
import delivery.core.application.ports.input.commands.OrderAssignmentUseCase
import delivery.core.domain.services.DispatchError
import io.mockk.every
import io.mockk.verify
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(OrderAssignmentController::class)
class OrderAssignmentControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @MockkBean
    private lateinit var useCase: OrderAssignmentUseCase

    @Test
    fun `assign order`() {
        every { useCase.assignTo() } returns Either.Right(Unit)

        mockMvc.perform(post("/api/v1/orders/assign"))
            .andExpect(status().isOk)

        verify { useCase.assignTo() }
    }

    @Test
    fun `fails to assign order when no orders found`() {
        every { useCase.assignTo() } returns Either.Left(OrderAssignmentError.OrderNotFound)

        mockMvc.perform(post("/api/v1/orders/assign"))
            .andExpect(status().isNotFound)
            .andExpect { content().string("Any new order not found") }

        verify { useCase.assignTo() }
    }

    @Test
    fun `fails to assign order when no couriers found`() {
        every { useCase.assignTo() } returns Either.Left(DispatchError.NoAvailableCourier)

        mockMvc.perform(post("/api/v1/orders/assign"))
            .andExpect(status().isNotFound)
            .andExpect { content().string("No available courier can take this order") }

        verify { useCase.assignTo() }
    }
}
