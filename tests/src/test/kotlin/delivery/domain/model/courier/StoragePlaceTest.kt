package delivery.domain.model.courier

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.UUID
import kotlin.test.Test

class StoragePlaceTest {

    @Test
    fun `creates storage with correct properties`() {
        val sp = StoragePlace.of(StoragePlaceType.BACKPACK).shouldBeRight()

        sp.type shouldBe StoragePlaceType.BACKPACK
        sp.totalVolume shouldBe 10
        sp.orderId shouldBe null
        sp.id shouldNotBe null
    }

    @Test
    fun `can store order if empty and fits`() {
        val sp = StoragePlace.of(StoragePlaceType.BACKPACK).shouldBeRight()

        sp.canStore(3) shouldBe StorageCheck.Ok
    }

    @Test
    fun `cannot store order if occupied`() {
        val sp = StoragePlace.of(StoragePlaceType.BICYCLE_TRUNK).shouldBeRight()
        val orderId = UUID.randomUUID()
        sp.store(orderId, 5).shouldBeRight()

        sp.canStore(3) shouldBe StorageCheck.Occupied
        sp.orderId shouldBe orderId
    }

    @Test
    fun `cannot store order if volume exceeds capacity`() {
        val sp = StoragePlace.of(StoragePlaceType.BACKPACK).shouldBeRight()

        sp.canStore(20) shouldBe StorageCheck.NotEnoughSpace
    }

    @Test
    fun `cannot store another order if already occupied`() {
        val sp = StoragePlace.of(StoragePlaceType.BICYCLE_TRUNK).shouldBeRight()
        val orderId = UUID.randomUUID()
        sp.store(orderId, 8).shouldBeRight()

        sp.orderId shouldBe orderId
        sp.canStore(2) shouldBe StorageCheck.Occupied
    }

    @Test
    fun `fails to store if occupied`() {
        // Arrange
        val sp = StoragePlace.of(StoragePlaceType.BACKPACK).shouldBeRight()
        val firstOrder = UUID.randomUUID()
        sp.store(firstOrder, 5).shouldBeRight()
        val secondOrder = UUID.randomUUID()

        // Act
        val result = sp.store(secondOrder, 5).shouldBeLeft()

        // Assert
        result shouldBe StorageError.Occupied
        result.message shouldBe "Storage is already occupied"
    }

    @Test
    fun `fails to store if volume exceeds capacity`() {
        // Arrange
        val sp = StoragePlace.of(StoragePlaceType.BACKPACK).shouldBeRight()

        // Act
        val result = sp.store(UUID.randomUUID(), 15).shouldBeLeft()

        // Assert
        result shouldBe StorageError.NotEnoughSpace
        result.message shouldBe "Order volume exceeds storage capacity"
    }

    @Test
    fun `clears order and frees storage`() {
        val sp = StoragePlace.of(StoragePlaceType.BICYCLE_TRUNK).shouldBeRight()
        val orderId = UUID.randomUUID()
        sp.store(orderId, 5).shouldBeRight()

        val extracted = sp.clear()

        extracted shouldBe orderId
        sp.canStore(5) shouldBe StorageCheck.Ok
        sp.orderId shouldBe null
    }
}
