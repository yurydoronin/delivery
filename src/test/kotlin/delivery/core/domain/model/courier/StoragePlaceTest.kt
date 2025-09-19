package delivery.core.domain.model.courier

import java.util.UUID
import kotlin.test.*

class StoragePlaceTest {

    @Test
    fun `a factory creates StoragePlace with correct properties`() {
        val sp = StoragePlace.of(StoragePlaceName.BACKPACK, 10)

        assertEquals(StoragePlaceName.BACKPACK, sp.name)
        assertEquals(10, sp.totalVolume)
        assertNull(sp.orderId)
        assertNotNull(sp.id)
    }

    @Test
    fun `a factory throws an exception if totalVolume is not positive`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            StoragePlace.of(StoragePlaceName.TRUNK, 0)
        }

        assertTrue(exception.message!!.contains("Total volume must be greater than 0"))
    }

    @Test
    fun `a storage is empty when no order is stored`() {
        val sp = StoragePlace.of(StoragePlaceName.BACKPACK, 5)

        assertTrue(sp.canStore(3))
    }

    @Test
    fun `returns false if a storage is occupied`() {
        val sp = StoragePlace.of(StoragePlaceName.TRUNK, 10)
        val orderId = UUID.randomUUID()
        sp.store(orderId, 5)

        assertFalse(sp.canStore(3))
        assertNotNull(sp.orderId)
        assertEquals(orderId, sp.orderId)
    }

    @Test
    fun `returns false if an order volume exceeds storage`() {
        val sp = StoragePlace.of(StoragePlaceName.BACKPACK, 5)

        assertFalse(sp.canStore(10))
    }

    @Test
    fun `puts order into an empty storage if volume fits`() {
        val sp = StoragePlace.of(StoragePlaceName.TRUNK, 10)
        val orderId = UUID.randomUUID()
        sp.store(orderId, 8)

        assertEquals(orderId, sp.orderId)
        assertFalse(sp.canStore(2))
    }

    @Test
    fun `throws an exception if a storage is occupied`() {
        val sp = StoragePlace.of(StoragePlaceName.BACKPACK, 10)
        val firstOrder = UUID.randomUUID()
        sp.store(firstOrder, 5)
        val secondOrder = UUID.randomUUID()

        val exception = assertFailsWith<IllegalArgumentException> {
            sp.store(secondOrder, 5)
        }

        assertTrue(exception.message!!.contains("Cannot put order: either storage is not empty or volume exceeded"))
    }

    @Test
    fun `throws an exception if an order volume exceeds totalVolume`() {
        val sp = StoragePlace.of(StoragePlaceName.BACKPACK, 10)

        val exception = assertFailsWith<IllegalArgumentException> {
            sp.store(UUID.randomUUID(), 15)
        }

        assertTrue(exception.message!!.contains("Cannot put order: either storage is not empty or volume exceeded"))
    }

    @Test
    fun `removes an order and makes a storage empty`() {
        val sp = StoragePlace.of(StoragePlaceName.TRUNK, 10)
        val orderId = UUID.randomUUID()
        sp.store(orderId, 5)

        val extracted = sp.clear()

        assertEquals(orderId, extracted)
        assertTrue(sp.canStore(5))
        assertNull(sp.orderId)
    }

}