package delivery.core.application.ports.output

import delivery.core.domain.model.courier.Courier
import java.util.UUID

interface CourierRepositoryPort {

    fun track(courier: Courier)
    fun get(courierId: UUID): Courier?
    fun findCouriersWithAnyFreeStorageForUpdate(): List<Courier>
    fun getAllCouriersForUpdate(): List<Courier>
}