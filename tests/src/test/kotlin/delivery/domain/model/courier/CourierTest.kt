package delivery.domain.model.courier

import delivery.domain.kernel.Location
import delivery.domain.kernel.LocationTestData
import delivery.domain.model.order.Order
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import java.util.UUID
import kotlin.test.Test

class CourierTest {

    @Test
    fun `creates courier with default storage`() {
        val startLocation = LocationTestData.random()
        val courier = Courier.of("John", 2, startLocation).shouldBeRight()

        courier.name shouldBe "John"
        courier.speed shouldBe 2
        courier.location shouldBe startLocation
        courier.storagePlaces.size shouldBe 1
        courier.storagePlaces.first().name shouldBe StoragePlaceName.BACKPACK
        courier.storagePlaces.first().totalVolume shouldBe 10
    }

    @Test
    fun `fails if name is blank`() {
        val result = Courier.of("", 2, LocationTestData.random()).shouldBeLeft()

        result shouldBe CourierError.InvalidName
        result.message shouldBe "Name must not be blank"
    }


    @Test
    fun `fails if speed is not positive`() {
        val result = Courier.of("John", 0, LocationTestData.random()).shouldBeLeft()

        result shouldBe CourierError.InvalidSpeed
        result.message shouldBe "Speed must be positive"
    }

    @Test
    fun `adds storage place`() {
        val courier = Courier.of("John", 2, LocationTestData.random()).shouldBeRight()

        courier.addStoragePlace(StoragePlaceName.BICYCLE_TRUNK, 20)

        courier.storagePlaces.size shouldBe 2
        courier.storagePlaces.any { it.name == StoragePlaceName.BICYCLE_TRUNK } shouldBe true
    }

    @Test
    fun `finds available storage for order`() {
        // Arrange
        val startLocation = LocationTestData.random()
        val courier = Courier.of("John", 2, startLocation).shouldBeRight()
        val storageDefault = courier.storagePlaces.first()
        val order = Order.of(UUID.randomUUID(), startLocation, 5).shouldBeRight()

        // Act
        val result = courier.findAvailableStorage(order).shouldBeRight()

        // Assert
        result shouldBe storageDefault
        result.canStore(order.volume) shouldBe StorageCheck.Ok
    }

    @Test
    fun `fails to find storage for large order`() {
        // Arrange
        val startLocation = LocationTestData.random()
        val courier = Courier.of("John", 2, startLocation).shouldBeRight()
        val largeOrder = Order.of(UUID.randomUUID(), LocationTestData.random(), 15).shouldBeRight()

        // Act
        val result = courier.findAvailableStorage(largeOrder).shouldBeLeft()

        // Assert
        result shouldBe CourierError.NoAvailableStorage
        result.message shouldBe "No available storage for this order"
    }

    @Test
    fun `takes order successfully`() {
        // Arrange
        val courier = Courier.of("John", 2, LocationTestData.random()).shouldBeRight()
        val order = Order.of(UUID.randomUUID(), LocationTestData.random(), 5).shouldBeRight()

        // Act
        courier.takeOrder(order).shouldBeRight()

        // Assert
        val sp = courier.storagePlaces.first { it.orderId == order.id }
        sp.orderId shouldBe order.id
    }

    @Test
    fun `completes order`() {
        // Arrange
        val startLocation = LocationTestData.random()
        val courier = Courier.of("John", 2, startLocation).shouldBeRight()
        val order = Order.of(UUID.randomUUID(), LocationTestData.random(), 5).shouldBeRight()
        courier.takeOrder(order)

        // Act
        courier.delivered(order).shouldBeRight()

        // Assert
        courier.storagePlaces.all { it.orderId == null } shouldBe true
    }

    @Test
    fun `fails to complete order`() {
        // Arrange
        val courier = Courier.of("John", 2, LocationTestData.random()).shouldBeRight()
        val order = Order.of(UUID.randomUUID(), LocationTestData.random(), 5).shouldBeRight()

        // Act
        val result = courier.delivered(order).shouldBeLeft()

        // Assert
        result shouldBe CourierError.OrderNotFound
        result.message shouldBe "Order not found in any storage"
    }

    @Test
    fun `calculates time to location`() {
        val courier = Courier.of("John", 2, Location.restore(1, 1)).shouldBeRight()
        val target = Location.of(5, 5).shouldBeRight()

        val result = courier.calculateTimeToLocation(target)

        result shouldBe 4 // distance 8, speed 2 → 4 steps
    }

    @Test
    fun `moves towards target`() {
        val courier = Courier.of("John", 2, Location.restore(1, 1)).shouldBeRight()
        val target = Location.of(5, 5).shouldBeRight()

        courier.move(target).shouldBeRight()

        courier.location shouldBe Location.restore(3, 1) // speed 2 → moves (2,2)
    }
}
