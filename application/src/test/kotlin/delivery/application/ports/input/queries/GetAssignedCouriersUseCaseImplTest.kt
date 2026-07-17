package delivery.application.ports.input.queries

import arrow.core.left
import delivery.application.ports.input.BaseRepositoryTest
import delivery.domain.kernel.Location
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
        val result = sut.execute()

        // Assert
        val couriers = result.shouldBeRight()

        couriers.size shouldBe 1
        couriers.first().courierId shouldBe courier1Id
        couriers.first().name shouldBe "Маша"
        couriers.first().location shouldBe Location.of(1, 1)
    }

    @Test
    fun `fails when no assigned couriers`() {
        val result = sut.execute()

        result shouldBe AssignedCouriersError.NoAssignedCouriers.left()
    }
}
