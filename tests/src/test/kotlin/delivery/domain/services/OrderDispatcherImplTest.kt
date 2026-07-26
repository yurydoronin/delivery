package delivery.domain.services

import delivery.domain.kernel.Location
import delivery.domain.model.courier.Courier
import delivery.domain.model.courier.StoragePlaceName
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
        val courier1 = Courier.of("Alice", 1, Location.restore(1, 1)).shouldBeRight() // медленный
        val courier2 = Courier.of("Bob", 2, Location.restore(1, 1)).shouldBeRight()   // быстрый
        val courier3 = Courier.of("Mike", 2, Location.restore(10, 10)).shouldBeRight() // далеко

        // Act
        val winner = OrderDispatcherImpl().dispatch(order, listOf(courier1, courier2, courier3)).shouldBeRight()

        // Assert
        winner.id shouldBe courier2.id
        winner.name shouldBe "Bob"
        winner.id shouldBe order.courierId
        order.status shouldBe OrderStatus.ASSIGNED
    }

    @Test
    fun `chooses courier with available storage when faster couriers are full`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.restore(5, 5), 15).shouldBeRight() // слишком большой
        val courier1 = Courier.of("Alice", 1, Location.restore(1, 1)).shouldBeRight() // медленный
        val courier2 = Courier.of("Bob", 2, Location.restore(1, 1)).shouldBeRight() // быстрейший, но нет места
        val courier3 =
            Courier.of("Mike", 2, Location.restore(10, 10)).shouldBeRight() // далеко, но есть доп место (в багажнике)
        courier3.addStoragePlace(StoragePlaceName.BICYCLE_TRUNK, 20)

        // Act
        val winner = OrderDispatcherImpl().dispatch(order, listOf(courier1, courier2, courier3)).shouldBeRight()

        // Assert
        winner.id shouldBe courier3.id
        winner.name shouldBe "Mike"
        winner.id shouldBe order.courierId
        order.status shouldBe OrderStatus.ASSIGNED
    }

    @Test
    fun `fails to dispatch if no available courier`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.restore(5, 5), 15).shouldBeRight()
        val courier1 = Courier.of("Alice", 1, Location.restore(1, 1)).shouldBeRight()
        val courier2 = Courier.of("Bob", 2, Location.restore(1, 1)).shouldBeRight()
        val courier3 = Courier.of("Mike", 2, Location.restore(10, 10)).shouldBeRight()

        // Act
        val result = OrderDispatcherImpl().dispatch(order, listOf(courier1, courier2, courier3)).shouldBeLeft()

        // Assert
        result shouldBe DispatchError.NoAvailableCourier
        result.message shouldBe "No available courier can take this order"
    }
}
