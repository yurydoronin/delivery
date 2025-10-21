package delivery.core.domain.model.courier

import arrow.core.raise.either
import delivery.core.domain.kernel.Location
import delivery.core.domain.model.order.Order
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CourierTest {

    @Test
    fun `creates courier with default storage`() {
        either {
            val startLocation = Location.of(1, 1)
            val courier = Courier.of("John", 2, startLocation).bind()

            assertEquals("John", courier.name)
            assertEquals(2, courier.speed)
            assertEquals(startLocation, courier.location)
            assertEquals(1, courier.storagePlaces.size)
            assertEquals(StoragePlaceName.BACKPACK, courier.storagePlaces.first().name)
            assertEquals(10, courier.storagePlaces.first().totalVolume)
        }
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
        either {
            val courier = Courier.of("John", 2, Location.of(1, 1)).bind()

            courier.addStoragePlace(StoragePlaceName.BICYCLE_TRUNK, 20)

            assertEquals(2, courier.storagePlaces.size)
            assertTrue(courier.storagePlaces.any { it.name == StoragePlaceName.BICYCLE_TRUNK })
        }
    }

    @Test
    fun `finds available storage for order`() {
        either {
            // Arrange
            val startLocation = Location.of(1, 1)
            val courier = Courier.of("John", 2, startLocation).bind()
            val storageDefault = courier.storagePlaces.first()
            val order = Order.of(UUID.randomUUID(), startLocation, 5)

            // Act
            val result = courier.findAvailableStorage(order)

            // Assert
            val storage = result.shouldBeRight()
            storage shouldBe storageDefault
            storage.canStore(order.volume).shouldBeInstanceOf<StorageCheck.Ok>()
        }
    }

    @Test
    fun `fails to find storage for large order`() {
        either {
            // Arrange
            val startLocation = Location.of(1, 1)
            val courier = Courier.of("John", 2, startLocation).bind()
            val largeOrder = Order.of(UUID.randomUUID(), Location.of(1, 1), 15)

            // Act
            val result = courier.findAvailableStorage(largeOrder)

            // Assert
            val error = result.shouldBeLeft()
            error shouldBe CourierError.NoAvailableStorage
            error.message shouldBe "No available storage for this order"
        }
    }

    @Test
    fun `takes order successfully`() {
        either {
            // Arrange
            val courier = Courier.of("John", 2, Location.of(1, 1)).bind()
            val order = Order.of(UUID.randomUUID(), Location.of(1, 1), 5)

            // Act
            val result = courier.takeOrder(order)

            // Assert
            result.shouldBeRight()
            val place = courier.storagePlaces.first { it.orderId == order.id }
            assertEquals(order.id, place.orderId)
        }
    }

    @Test
    fun `completes order`() {
        either {
            // Arrange
            val startLocation = Location.of(1, 1)
            val courier = Courier.of("John", 2, startLocation).bind()
            val order = Order.of(UUID.randomUUID(), Location.of(1, 1), 5)
            courier.takeOrder(order)

            // Act
            val result = courier.completeOrder(order)

            // Assert
            result.shouldBeRight()
            assertTrue(courier.storagePlaces.all { it.orderId == null })
        }
    }

    @Test
    fun `fails to complete order`() {
        either {
            // Arrange
            val courier = Courier.of("John", 2, Location.of(1, 1)).bind()
            val order = Order.of(UUID.randomUUID(), Location.of(1, 1), 5)

            // Act
            val result = courier.completeOrder(order)

            // Assert
            val error = result.shouldBeLeft()
            error shouldBe CourierError.OrderNotFound
            error.message shouldBe "Order not found in any storage"
        }
    }

    @Test
    fun `calculates time to location`() {
        either {
            val courier = Courier.of("John", 2, Location.of(1, 1)).bind()
            val target = Location.of(5, 5)

            val time = courier.calculateTimeToLocation(target)

            assertEquals(4, time) // distance 8, speed 2 → 4 steps
        }
    }

    @Test
    fun `moves towards target`() {
        either {
            val courier = Courier.of("John", 2, Location.of(1, 1)).bind()
            val target = Location.of(5, 5)

            courier.move(target)

            assertEquals(Location.of(3, 1), courier.location) // speed 2 → moves (2,2)
        }
    }
}
