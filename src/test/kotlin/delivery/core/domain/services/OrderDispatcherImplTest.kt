package delivery.core.domain.services

import delivery.core.domain.kernel.Location
import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.courier.StoragePlaceName
import delivery.core.domain.model.order.Order
import delivery.core.domain.model.order.OrderStatus
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import java.util.UUID
import kotlin.test.Test

class OrderDispatcherTest {

    @Test
    fun `assigns order to fastest available courier`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.of(5, 5), 5)
        val courier1 = Courier.of("Alice", 1, Location.of(1, 1)) // медленный
        val courier2 = Courier.of("Bob", 2, Location.of(1, 1))   // быстрый
        val courier3 = Courier.of("Mike", 2, Location.of(10, 10)) // далеко

        // Act
        val result = OrderDispatcherImpl().dispatch(order, listOf(courier1, courier2, courier3))

        // Assert
        val winner = result.shouldBeRight()
        winner.id shouldBe courier2.id
        winner.name shouldBe "Bob"
        winner.id shouldBe order.courierId
        OrderStatus.ASSIGNED shouldBe order.status
    }

    @Test
    fun `chooses courier with available storage when faster couriers are full`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.of(5, 5), 15) // слишком большой
        val courier1 = Courier.of("Alice", 1, Location.of(1, 1)) // медленный
        val courier2 = Courier.of("Bob", 2, Location.of(1, 1)) // быстрейший, но нет места
        val courier3 = Courier.of("Mike", 2, Location.of(10, 10)) // далеко, но есть доп место (в багажнике)
        courier3.addStoragePlace(StoragePlaceName.TRUNK, 20)

        // Act
        val result = OrderDispatcherImpl().dispatch(order, listOf(courier1, courier2, courier3))

        // Assert
        val winner = result.shouldBeRight()
        winner.id shouldBe courier3.id
        winner.name shouldBe "Mike"
        winner.id shouldBe order.courierId
        OrderStatus.ASSIGNED shouldBe order.status
    }

    @Test
    fun `fails to dispatch if no available courier`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.of(5, 5), 15)
        val courier1 = Courier.of("Alice", 1, Location.of(1, 1))
        val courier2 = Courier.of("Bob", 2, Location.of(1, 1))
        val courier3 = Courier.of("Mike", 2, Location.of(10, 10))

        // Act
        val result = OrderDispatcherImpl().dispatch(order, listOf(courier1, courier2, courier3))

        // Assert
        val error = result.shouldBeLeft()
        error shouldBe DispatchError.NoAvailableCourier
        error.message shouldBe "No available courier can take this order"
    }
}
