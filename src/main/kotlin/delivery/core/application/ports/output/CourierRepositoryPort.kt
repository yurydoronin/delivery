package delivery.core.application.ports.output

import delivery.core.domain.model.courier.Courier
import java.util.UUID

interface CourierRepositoryPort {

    fun add(courier: Courier)
    fun update(courier: Courier)
    fun get(courierId: UUID): Courier?
    fun getAvailableCouriers(): List<Courier>
}