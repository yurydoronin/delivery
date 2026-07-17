package delivery.application.ports.output

import delivery.domain.model.courier.Courier
import java.util.UUID

interface CourierRepositoryPort {

    fun track(courier: Courier)
    fun get(courierId: UUID): Courier?
    fun findCouriersWithAnyFreeStorage(): List<Courier>
    fun getCouriersWithAssignedOrders(): List<Courier>
}