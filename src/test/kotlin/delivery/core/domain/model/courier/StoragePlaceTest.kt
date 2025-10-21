package delivery.core.domain.model.courier

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.matchers.shouldBe
import java.util.UUID
import kotlin.test.*

class StoragePlaceTest {

    @Test
    fun `creates storage with correct properties`() {
        val sp = StoragePlace.of(StoragePlaceName.BACKPACK, 10)

        assertEquals(StoragePlaceName.BACKPACK, sp.name)
        assertEquals(10, sp.totalVolume)
        assertNull(sp.orderId)
        assertNotNull(sp.id)
    }

    @Test
    fun `fails if total volume is not positive`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            StoragePlace.of(StoragePlaceName.BICYCLE_TRUNK, 0)
        }

        assertTrue(exception.message!!.contains("Total volume must be greater than 0"))
    }

    @Test
    fun `can store order if empty and fits`() {
        val sp = StoragePlace.of(StoragePlaceName.BACKPACK, 5)

        assertTrue(sp.canStore(3) is StorageCheck.Ok)
    }

    @Test
    fun `cannot store order if occupied`() {
        val sp = StoragePlace.of(StoragePlaceName.BICYCLE_TRUNK, 10)
        val orderId = UUID.randomUUID()
        sp.store(orderId, 5)

        val check = sp.canStore(3)

        assertTrue(check is StorageCheck.Occupied)
        assertEquals(orderId, sp.orderId)
    }

    @Test
    fun `cannot store order if volume exceeds capacity`() {
        val sp = StoragePlace.of(StoragePlaceName.BACKPACK, 5)

        val check = sp.canStore(10)

        assertTrue(check is StorageCheck.NotEnoughSpace)
    }

    @Test
    fun `cannot store another order if already occupied`() {
        val sp = StoragePlace.of(StoragePlaceName.BICYCLE_TRUNK, 10)
        val orderId = UUID.randomUUID()
        sp.store(orderId, 8)

        assertEquals(orderId, sp.orderId)
        assertTrue(sp.canStore(2) is StorageCheck.Occupied)
    }

    @Test
    fun `fails to store if occupied`() {
        // Arrange
        val sp = StoragePlace.of(StoragePlaceName.BACKPACK, 10)
        val firstOrder = UUID.randomUUID()
        sp.store(firstOrder, 5)
        val secondOrder = UUID.randomUUID()

        // Act
        val result = sp.store(secondOrder, 5)

        // Assert
        val error = result.shouldBeLeft()
        error shouldBe StorageError.Occupied
        error.message shouldBe "Storage is already occupied"
    }

    @Test
    fun `fails to store if volume exceeds capacity`() {
        // Arrange
        val sp = StoragePlace.of(StoragePlaceName.BACKPACK, 10)

        // Act
        val result = sp.store(UUID.randomUUID(), 15)

        // Assert
        val error = result.shouldBeLeft()
        error shouldBe StorageError.NotEnoughSpace
        error.message shouldBe "Order volume exceeds storage capacity"
    }

    @Test
    fun `clears order and frees storage`() {
        val sp = StoragePlace.of(StoragePlaceName.BICYCLE_TRUNK, 10)
        val orderId = UUID.randomUUID()
        sp.store(orderId, 5)

        val extracted = sp.clear()

        assertEquals(orderId, extracted)
        assertTrue(sp.canStore(5) is StorageCheck.Ok)
        assertNull(sp.orderId)
    }
}