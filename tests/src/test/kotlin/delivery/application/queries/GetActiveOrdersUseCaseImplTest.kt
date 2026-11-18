package delivery.application.queries

import delivery.BaseRepositoryTest
import delivery.application.ports.input.queries.ActiveOrdersError
import delivery.application.ports.input.queries.GetActiveOrdersUseCaseImpl
import delivery.domain.kernel.LocationTestData
import delivery.domain.model.courier.CourierType
import delivery.domain.model.courier.StoragePlaceType
import delivery.domain.model.order.Order
import delivery.domain.model.order.OrderStatus
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.simple.JdbcClient

class GetActiveOrdersUseCaseImplTest @Autowired constructor(
    private val jdbcClient: JdbcClient,
    private val sut: GetActiveOrdersUseCaseImpl,
) : BaseRepositoryTest() {

    @Test
    fun `get active orders`() {
        // Arrange
        val courierId = UUID.randomUUID()

        jdbcClient.sql(
            """
            INSERT INTO couriers(id, type, speed, location_x, location_y)
            VALUES (:id, :type, :speed, :locationX, :locationY)
            """.trimIndent()
        )
            .param("id", courierId)
            .param("type", CourierType.WALKING.name)
            .param("speed", 4)
            .param("locationX", 1)
            .param("locationY", 1)
            .update()

        jdbcClient.sql(
            """
            INSERT INTO storage_places(id, type, total_volume, order_id, courier_id)
            VALUES (:id, :type, :totalVolume, :orderId, :courierId)
            """.trimIndent()
        )
            .param("id", UUID.randomUUID())
            .param("type", StoragePlaceType.BACKPACK.name)
            .param("totalVolume", StoragePlaceType.BACKPACK.volume)
            .param("orderId", null)
            .param("courierId", courierId)
            .update()

        val order1 = Order.with(UUID.randomUUID(), LocationTestData.random(), 4).shouldBeRight()
        val order2 = Order.with(UUID.randomUUID(), LocationTestData.random(), 7).shouldBeRight()
        val completedOrder = Order.with(UUID.randomUUID(), LocationTestData.random(), 5).shouldBeRight()

        order1.assignToCourierWith(courierId)

        insertOrder(order1)
        insertOrder(order2)
        insertOrder(completedOrder, OrderStatus.COMPLETED)

        // Act
        val result = sut.execute().shouldBeRight()

        // Assert
        result.size shouldBe 2
        order1.status shouldBe OrderStatus.ASSIGNED
        order2.status shouldBe OrderStatus.CREATED
    }

    @Test
    fun `fails when no incomplete orders`() {
        val result = sut.execute().shouldBeLeft()

        result shouldBe ActiveOrdersError.NoActiveOrders
    }

    private fun insertOrder(
        order: Order,
        status: OrderStatus = order.status,
    ) {
        jdbcClient.sql(
            """
            INSERT INTO orders(id, location_x, location_y, volume, status, courier_id)
            VALUES (:id, :locationX, :locationY, :volume, :status, :courierId)
            """.trimIndent()
        )
            .param("id", order.id)
            .param("locationX", order.location.x)
            .param("locationY", order.location.y)
            .param("volume", order.volume)
            .param("status", status.name)
            .param("courierId", order.courierId)
            .update()
    }
}
