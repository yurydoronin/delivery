package delivery.api.input.adapters.http

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import delivery.core.application.ports.input.queries.AssignedCouriersError
import delivery.core.application.ports.input.queries.GetAssignedCouriersResult
import delivery.core.application.ports.input.queries.GetAssignedCouriersUseCase
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

@WebMvcTest(GetAssignedCouriersController::class)
class GetAssignedCouriersControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {
    @MockkBean
    private lateinit var useCase: GetAssignedCouriersUseCase

    @Test
    fun `get assigned couriers`() {
        // Arrange
        val courierId = UUID.randomUUID()
        val result = GetAssignedCouriersResult(courierId, "Вася", Location.of(1, 1))

        every { useCase.execute() } returns Either.Right(listOf(result))

        val expectedJson = objectMapper.writeValueAsString(
            listOf(
                GetAssignedCouriersResponse(courierId, "Вася", CourierLocationResponse(1, 1))
            )
        )

        // Act & Assert
        mockMvc.perform(
            get("/api/v1/couriers/assigned")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().json(expectedJson))
    }

    @Test
    fun `fails to get assigned couriers`() {
        // Arrange
        every { useCase.execute() } returns Either.Left(AssignedCouriersError.NoAssignedCouriers)

        // Act & Assert
        mockMvc.perform(
            get("/api/v1/couriers/assigned")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound)
    }
}
