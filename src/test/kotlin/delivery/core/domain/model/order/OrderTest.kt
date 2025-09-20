package delivery.core.domain.model.order

import delivery.core.domain.kernel.Location
import java.util.UUID
import kotlin.test.*

class OrderTest {

    @Test
    fun `creates order with correct properties`() {
        val id = UUID.randomUUID()
        val location = Location.of(1, 1)

        val order = Order.of(id, location, 5)

        assertEquals(id, order.id)
        assertEquals(location, order.location)
        assertEquals(5, order.volume)
        assertEquals(OrderStatus.CREATED, order.status)
        assertNull(order.courierId)
    }

    @Test
    fun `fails if volume is not positive`() {
        val id = UUID.randomUUID()
        val location = Location.of(1, 1)

        val exception = assertFailsWith<IllegalArgumentException> {
            Order.of(id, location, 0)
        }

        assertTrue(exception.message!!.contains("Volume must be positive"))
    }

    @Test
    fun `assigns order to courier`() {
        val order = Order.of(UUID.randomUUID(), Location.of(1, 1), 5)
        val courierId = UUID.randomUUID()

        order.assignToCourier(courierId)

        assertEquals(OrderStatus.ASSIGNED, order.status)
        assertEquals(courierId, order.courierId)
    }

    @Test
    fun `fails when assigning already assigned order`() {
        val order = Order.of(UUID.randomUUID(), Location.of(1, 1), 5)
        val courierId1 = UUID.randomUUID()
        val courierId2 = UUID.randomUUID()

        order.assignToCourier(courierId1)

        val exception = assertFailsWith<IllegalArgumentException> {
            order.assignToCourier(courierId2)
        }

        assertEquals(OrderStatus.ASSIGNED, order.status)
        assertTrue(exception.message!!.contains("Only orders in CREATED status can be assigned"))
    }

    @Test
    fun `completes assigned order`() {
        val order = Order.of(UUID.randomUUID(), Location.of(1, 1), 5)
        val courierId = UUID.randomUUID()
        order.assignToCourier(courierId)

        order.complete()

        assertEquals(OrderStatus.COMPLETED, order.status)
        assertEquals(courierId, order.courierId)
    }

    @Test
    fun `fails when completing non-assigned order`() {
        val order = Order.of(UUID.randomUUID(), Location.of(1, 1), 5)

        val exception = assertFailsWith<IllegalArgumentException> {
            order.complete()
        }

        assertEquals(OrderStatus.CREATED, order.status)
        assertTrue(exception.message!!.contains("Only assigned orders can be completed"))
    }
}
