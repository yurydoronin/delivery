package delivery.core.domain.model.courier

import delivery.core.domain.kernel.Location
import delivery.core.domain.model.order.Order
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CourierTest {

    @Test
    fun `creates courier with default storage`() {
        val startLocation = Location.of(1, 1)
        val courier = Courier.of("John", 2, startLocation)

        assertEquals("John", courier.name)
        assertEquals(2, courier.speed)
        assertEquals(startLocation, courier.location)
        assertEquals(1, courier.storagePlaces.size)
        assertEquals(StoragePlaceName.BACKPACK, courier.storagePlaces.first().name)
        assertEquals(10, courier.storagePlaces.first().totalVolume)
    }

    @Test
    fun `fails if name is blank`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Courier.of("", 2, Location.of(1, 1))
        }

        assertTrue(exception.message!!.contains("Name must not be blank"))
    }

    @Test
    fun `fails if speed is not positive`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Courier.of("John", 0, Location.of(1, 1))
        }

        assertTrue(exception.message!!.contains("Speed must be positive"))
    }

    @Test
    fun `adds storage place`() {
        val courier = Courier.of("John", 2, Location.of(1, 1))

        courier.addStoragePlace(StoragePlaceName.TRUNK, 20)

        assertEquals(2, courier.storagePlaces.size)
        assertTrue(courier.storagePlaces.any { it.name == StoragePlaceName.TRUNK })
    }

    @Test
    fun `finds available storage for order`() {
        val startLocation = Location.of(1, 1)
        val courier = Courier.of("John", 2, startLocation)
        val storageDefault = courier.storagePlaces.first()
        val order = Order.of(UUID.randomUUID(), startLocation, 5)

        val storage = courier.findAvailableStorage(order)

        assertTrue(storage.canStore(order.volume) is StorageCheck.Ok)
        assertEquals(storageDefault, storage)

    }

    @Test
    fun `fails to take order if no storage available`() {
        val startLocation = Location.of(1, 1)
        val courier = Courier.of("John", 2, startLocation)
        val largeOrder = Order.of(UUID.randomUUID(), Location.of(1, 1), 15)

        val exception = assertFailsWith<IllegalArgumentException> {
            courier.takeOrder(largeOrder)
        }
        assertTrue(exception.message!!.contains("No available storage"))
    }

    @Test
    fun `takes order`() {
        val startLocation = Location.of(1, 1)
        val courier = Courier.of("John", 2, startLocation)
        val order = Order.of(UUID.randomUUID(), Location.of(1, 1), 5)

        courier.takeOrder(order)

        val place = courier.storagePlaces.first { it.orderId == order.id }
        assertEquals(order.id, place.orderId)
    }

    @Test
    fun `completes order`() {
        val startLocation = Location.of(1, 1)
        val courier = Courier.of("John", 2, startLocation)
        val order = Order.of(UUID.randomUUID(), Location.of(1, 1), 5)
        courier.takeOrder(order)

        courier.completeOrder(order)

        assertTrue(courier.storagePlaces.all { it.orderId == null })
    }

    @Test
    fun `fails to complete order`() {
        val courier = Courier.of("John", 2, Location.of(1, 1))
        val order = Order.of(UUID.randomUUID(), Location.of(1, 1), 5)

        val exception = assertFailsWith<IllegalArgumentException> {
            courier.completeOrder(order)
        }
        assertTrue(exception.message!!.contains("Order not found in any storage"))
    }

    @Test
    fun `calculates time to location`() {
        val courier = Courier.of("John", 2, Location.of(1, 1))
        val target = Location.of(5, 5)

        val time = courier.calculateTimeToLocation(target)

        assertEquals(4, time) // distance 8, speed 2 → 4 steps
    }

    @Test
    fun `moves towards target`() {
        val courier = Courier.of("John", 2, Location.of(1, 1))
        val target = Location.of(5, 5)

        courier.move(target)

        assertEquals(Location.of(3, 1), courier.location) // speed 2 → moves (2,2)
    }
}
