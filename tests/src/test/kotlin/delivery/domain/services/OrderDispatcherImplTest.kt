package delivery.domain.services

import delivery.domain.kernel.Location
import delivery.domain.model.courier.Courier
import delivery.domain.model.courier.CourierType
import delivery.domain.model.order.Order
import delivery.domain.model.order.OrderStatus
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import java.util.UUID
import kotlin.test.Test

class OrderDispatcherTest {

    @Test
    fun `assigns order to fastest available courier`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.restore(5, 5), 5).shouldBeRight()
        val courier1 = Courier.of(CourierType.WALKING, 1, Location.restore(1, 1)).shouldBeRight() // медленный
        val courier2 = Courier.of(CourierType.BICYCLE, 2, Location.restore(1, 1)).shouldBeRight()   // быстрый
        val courier3 = Courier.of(CourierType.CAR, 2, Location.restore(10, 10)).shouldBeRight() // далеко

        // Act
        val winner = OrderDispatcherImpl().dispatch(order, listOf(courier1, courier2, courier3)).shouldBeRight()

        // Assert
        winner.id shouldBe courier2.id
        winner.type shouldBe CourierType.BICYCLE
        winner.id shouldBe order.courierId
        order.status shouldBe OrderStatus.ASSIGNED
    }

    @Test
    fun `chooses courier with available storage when faster couriers are full`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.restore(5, 5), 40).shouldBeRight()
        val courier1 = Courier.of(CourierType.WALKING, 1, Location.restore(1, 1)).shouldBeRight() // медленный
        val courier2 =
            Courier.of(CourierType.BICYCLE, 2, Location.restore(1, 1)).shouldBeRight() // быстрейший, но нет места
        val courier3 = Courier.of(CourierType.CAR, 2, Location.restore(10, 10))
            .shouldBeRight() // далеко, но есть доп место (в багажнике)

        // Act
        val winner = OrderDispatcherImpl().dispatch(order, listOf(courier1, courier2, courier3)).shouldBeRight()

        // Assert
        winner.id shouldBe courier3.id
        winner.type shouldBe CourierType.CAR
        order.courierId shouldBe courier3.id
        order.status shouldBe OrderStatus.ASSIGNED
    }

    @Test
    fun `fails to dispatch when no courier has enough storage`() {
        val order = Order.of(UUID.randomUUID(), Location.restore(5, 5), 20).shouldBeRight()
        val courier = Courier.of(CourierType.WALKING, 1, Location.restore(1, 1)).shouldBeRight()

        val result = OrderDispatcherImpl()
            .dispatch(order, listOf(courier))
            .shouldBeLeft()

        result shouldBe DispatchError.NoAvailableCourier
    }

    @Test
    fun `fails to dispatch order when order status is not created`() {
        val order = Order.of(UUID.randomUUID(), Location.restore(5, 5), 20).shouldBeRight()
        val courier = Courier.of(CourierType.WALKING, 1, Location.restore(1, 1)).shouldBeRight()
        order.assignToCourier(courier.id)

        val result = OrderDispatcherImpl()
            .dispatch(order, listOf(courier))
            .shouldBeLeft()

        result shouldBe DispatchError.InvalidOrderStatusForAssignment
        order.status shouldBe OrderStatus.ASSIGNED
    }
}
