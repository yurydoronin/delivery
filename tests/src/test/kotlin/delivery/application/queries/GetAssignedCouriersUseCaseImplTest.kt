package delivery.application.queries

import delivery.BaseRepositoryTest
import delivery.application.ports.input.queries.AssignedCouriersError
import delivery.application.ports.input.queries.GetAssignedCouriersUseCaseImpl
import delivery.common.types.dto.LocationResult
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import java.util.UUID
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

class GetAssignedCouriersUseCaseImplTest @Autowired constructor(
    private val jdbcTemplate: JdbcTemplate,
    private val sut: GetAssignedCouriersUseCaseImpl
) : BaseRepositoryTest() {

    @Test
    fun `get assigned couriers`() {
        // Arrange
        val courier1Id = UUID.randomUUID()
        val courier2Id = UUID.randomUUID()

        jdbcTemplate.update(
            """
        INSERT INTO couriers(
            id,
            name,
            speed,
            location_x,
            location_y
        )
        VALUES (?, ?, ?, ?, ?)
        """.trimIndent(),
            courier1Id, "Маша", 4, 1, 1
        )

        jdbcTemplate.update(
            """
        INSERT INTO couriers(
            id,
            name,
            speed,
            location_x,
            location_y
        )
        VALUES (?, ?, ?, ?, ?)
        """.trimIndent(),
            courier2Id, "Коля", 1, 2, 2
        )

        jdbcTemplate.update(
            """
        INSERT INTO orders(
            id,
            location_x,
            location_y,
            volume,
            status,
            courier_id
        )
        VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent(),
            UUID.randomUUID(), 3, 3, 1, "ASSIGNED", courier1Id
        )

        // Act
        val result = sut.execute().shouldBeRight()

        // Assert
        result.size shouldBe 1
        result.first().courierId shouldBe courier1Id
        result.first().name shouldBe "Маша"
        result.first().location shouldBe LocationResult(1, 1)
    }

    @Test
    fun `fails when no assigned couriers`() {
        val result = sut.execute().shouldBeLeft()

        result shouldBe AssignedCouriersError.NoAssignedCouriers
    }
}
