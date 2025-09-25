package delivery.core.domain.services

import delivery.core.domain.kernel.Location
import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.courier.StoragePlaceName
import delivery.core.domain.model.order.Order
import delivery.core.domain.model.order.OrderStatus
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OrderDispatcherTest {

    @Test
    fun `assigns order to fastest available courier`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.of(5, 5), 5)
        val courier1 = Courier.of("Alice", 1, Location.of(1, 1)) // медленный
        val courier2 = Courier.of("Bob", 2, Location.of(1, 1))   // быстрый
        val courier3 = Courier.of("Mike", 2, Location.of(10, 10)) // далеко

        // Act
        val winner = OrderDispatcherImpl().dispatch(order, listOf(courier1, courier2, courier3))

        // Assert
        assertEquals(courier2.id, winner.id)
        assertEquals(courier2.name, "Bob")
        assertEquals(OrderStatus.ASSIGNED, order.status)
        assertEquals(winner.id, order.courierId)
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
        val winner = OrderDispatcherImpl().dispatch(order, listOf(courier1, courier2, courier3))

        // Assert
        assertEquals(courier3.id, winner.id)
        assertEquals(courier3.name, "Mike")
        assertEquals(OrderStatus.ASSIGNED, order.status)
        assertEquals(winner.id, order.courierId)
    }

    @Test
    fun `fails to dispatch if no courier can take order`() {
        // Arrange
        val order = Order.of(UUID.randomUUID(), Location.of(5, 5), 15)
        val courier1 = Courier.of("Alice", 1, Location.of(1, 1))
        val courier2 = Courier.of("Bob", 2, Location.of(1, 1))
        val courier3 = Courier.of("Mike", 2, Location.of(10, 10))

        // Act
        val exception = assertFailsWith<IllegalArgumentException> {
            OrderDispatcherImpl().dispatch(order, listOf(courier1, courier2, courier3))
        }
        // Assert
        assertTrue(exception.message!!.contains("No available courier"))
    }
}
