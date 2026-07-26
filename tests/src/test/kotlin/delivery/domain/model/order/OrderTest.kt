package delivery.domain.model.order

import delivery.domain.kernel.LocationTestData
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import java.util.UUID
import kotlin.test.Test

class OrderTest {

    @Test
    fun `creates order with correct properties`() {
        val id = UUID.randomUUID()
        val location = LocationTestData.random()

        val order = Order.of(id, location, 5).shouldBeRight()

        order.id shouldBe id
        order.location shouldBe location
        order.volume shouldBeExactly 5
        order.status shouldBe OrderStatus.CREATED
        order.courierId shouldBe null
    }

    @Test
    fun `fails if volume is not positive`() {
        val id = UUID.randomUUID()
        val location = LocationTestData.random()

        val result = Order.of(id, location, 0).shouldBeLeft()

        result shouldBe OrderError.InvalidVolume
        result.message shouldBe "Volume must be positive"
    }

    @Test
    fun `assigns order to courier`() {
        val order = Order.of(UUID.randomUUID(), LocationTestData.random(), 5).shouldBeRight()
        val courierId = UUID.randomUUID()

        order.assignToCourier(courierId)

        order.status shouldBe OrderStatus.ASSIGNED
        order.courierId shouldBe courierId
    }

    @Test
    fun `fails when assigning already assigned order`() {
        val order = Order.of(UUID.randomUUID(), LocationTestData.random(), 5).shouldBeRight()
        val courierId1 = UUID.randomUUID()
        val courierId2 = UUID.randomUUID()
        order.assignToCourier(courierId1)

        val result = order.assignToCourier(courierId2).shouldBeLeft()

        result shouldBe OrderError.InvalidStatusForAssignment
        order.status shouldBe OrderStatus.ASSIGNED
        result.message shouldBe "Only orders in CREATED status can be assigned"
    }

    @Test
    fun `completes assigned order`() {
        val order = Order.of(UUID.randomUUID(), LocationTestData.random(), 5).shouldBeRight()
        val courierId = UUID.randomUUID()
        order.assignToCourier(courierId)

        order.complete()

        order.status shouldBe OrderStatus.COMPLETED
        order.courierId shouldBe courierId
    }

    @Test
    fun `fails when completing non-assigned order`() {
        val order = Order.of(UUID.randomUUID(), LocationTestData.random(), 5).shouldBeRight()

        val result = order.complete().shouldBeLeft()

        result shouldBe OrderError.InvalidStatusForCompletion
        order.status shouldBe OrderStatus.CREATED
        result.message shouldBe "Only assigned orders can be completed"
    }
}
