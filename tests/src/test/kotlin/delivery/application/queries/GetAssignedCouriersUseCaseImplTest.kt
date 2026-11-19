package delivery.application.queries

import delivery.BaseRepositoryTest
import delivery.application.dto.CourierTypeResult
import delivery.application.dto.LocationResult
import delivery.application.ports.input.queries.AssignedCouriersError
import delivery.application.ports.input.queries.GetAssignedCouriersUseCaseImpl
import delivery.domain.model.courier.CourierType
import delivery.domain.model.courier.StoragePlaceType
import delivery.domain.model.courier.toStoragePlaceTypes
import delivery.domain.model.order.OrderStatus
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import java.util.UUID
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.simple.JdbcClient

class GetAssignedCouriersUseCaseImplTest @Autowired constructor(
    private val jdbcClient: JdbcClient,
    private val sut: GetAssignedCouriersUseCaseImpl
) : BaseRepositoryTest() {

    @Test
    fun `get assigned couriers`() {
        // Arrange
        val courier1Id = UUID.randomUUID()
        val courier2Id = UUID.randomUUID()

        val courier1Type = CourierType.WALKING
        val courier2Type = CourierType.BICYCLE

        insertCourier(courier1Id, courier1Type, speed = 4, locationX = 1, locationY = 1)
        courier1Type.toStoragePlaceTypes().forEach {
            insertStoragePlace(courier1Id, it)
        }

        insertCourier(courier2Id, courier2Type, speed = 1, locationX = 2, locationY = 2)
        courier2Type.toStoragePlaceTypes().forEach {
            insertStoragePlace(courier2Id, it)
        }

        insertAssignedOrder(courier1Id)

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

    private fun insertCourier(
        id: UUID,
        type: CourierType,
        speed: Int,
        locationX: Int,
        locationY: Int,
    ) {
        jdbcClient.sql(
            """
            INSERT INTO couriers(id, type, speed, location_x, location_y)
            VALUES (:id, :type, :speed, :locationX, :locationY)
            """.trimIndent()
        )
            .param("id", id)
            .param("type", type.name)
            .param("speed", speed)
            .param("locationX", locationX)
            .param("locationY", locationY)
            .update()
    }

    private fun insertStoragePlace(
        courierId: UUID,
        type: StoragePlaceType,
    ) {
        jdbcClient.sql(
            """
            INSERT INTO storage_places(id, type, total_volume, order_id, courier_id)
            VALUES (:id, :type, :totalVolume, :orderId, :courierId)
            """.trimIndent()
        )
            .param("id", UUID.randomUUID())
            .param("type", type.name)
            .param("totalVolume", type.volume)
            .param("orderId", null)
            .param("courierId", courierId)
            .update()
    }

    private fun insertAssignedOrder(courierId: UUID) {
        jdbcClient.sql(
            """
            INSERT INTO orders(id, location_x, location_y, volume, status, courier_id)
            VALUES (:id, :locationX, :locationY, :volume, :status, :courierId)
            """.trimIndent()
        )
            .param("id", UUID.randomUUID())
            .param("locationX", 3)
            .param("locationY", 3)
            .param("volume", 1)
            .param("status", OrderStatus.ASSIGNED.name)
            .param("courierId", courierId)
            .update()
    }
}
