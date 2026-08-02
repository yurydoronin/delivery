package delivery.api

import arrow.core.Either
import com.ninjasquad.springmockk.MockkBean
import delivery.api.input.adapters.http.GetAssignedCouriersController
import delivery.api.input.adapters.http.dto.CourierResponse
import delivery.api.input.adapters.http.dto.CourierTypeResponse
import delivery.api.input.adapters.http.dto.LocationResponse
import delivery.application.dto.CourierTypeResult
import delivery.application.dto.LocationResult
import delivery.application.ports.input.queries.AssignedCouriersError
import delivery.application.ports.input.queries.GetAssignedCouriersResult
import delivery.application.ports.input.queries.GetAssignedCouriersUseCase
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
        val result = GetAssignedCouriersResult(courierId, CourierTypeResult.WALKING, LocationResult(1, 1))

        every { useCase.execute() } returns Either.Right(listOf(result))

        val expectedJson = objectMapper.writeValueAsString(
            listOf(
                CourierResponse(courierId, CourierTypeResponse.WALKING, LocationResponse(1, 1))
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
