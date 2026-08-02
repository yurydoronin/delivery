package delivery.application.queries

import delivery.BaseRepositoryTest
import delivery.application.ports.input.queries.AssignedCouriersError
import delivery.application.ports.input.queries.GetAssignedCouriersUseCaseImpl
import delivery.application.dto.CourierTypeResult
import delivery.application.dto.LocationResult
import delivery.domain.model.courier.CourierType
import delivery.domain.model.courier.StoragePlaceType
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
            type,
            speed,
            location_x,
            location_y
        )
        VALUES (?, ?, ?, ?, ?)
        """.trimIndent(),
            courier1Id, CourierType.WALKING.name, 4, 1, 1
        )

        jdbcTemplate.update(
            """
            INSERT INTO storage_places(
                id,
                type,
                total_volume,
                order_id,
                courier_id
            )
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            UUID.randomUUID(),
            StoragePlaceType.BACKPACK.name,
            StoragePlaceType.BACKPACK.volume,
            null,
            courier1Id
        )

        jdbcTemplate.update(
            """
        INSERT INTO couriers(
            id,
            type,
            speed,
            location_x,
            location_y
        )
        VALUES (?, ?, ?, ?, ?)
        """.trimIndent(),
            courier2Id, CourierType.WALKING.name, 1, 2, 2
        )

        jdbcTemplate.update(
            """
            INSERT INTO storage_places(
                id,
                type,
                total_volume,
                order_id,
                courier_id
            )
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            UUID.randomUUID(),
            StoragePlaceType.BICYCLE_BACKPACK.name,
            StoragePlaceType.BICYCLE_BACKPACK.volume,
            null,
            courier2Id
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
        result.first().type shouldBe CourierTypeResult.WALKING
        result.first().location shouldBe LocationResult(1, 1)
    }

    @Test
    fun `fails when no assigned couriers`() {
        val result = sut.execute().shouldBeLeft()

        result shouldBe AssignedCouriersError.NoAssignedCouriers
    }
}
