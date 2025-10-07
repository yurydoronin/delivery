package delivery.infrastructure.output.adapters.postgres

import delivery.core.application.ports.output.AggregateTracker
import delivery.core.application.ports.output.CourierRepositoryPort
import delivery.core.domain.model.courier.Courier
import java.util.UUID
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class CourierRepository(
    private val aggregateTracker: AggregateTracker,
    private val repository: CourierJpaRepository
) : CourierRepositoryPort {

    override fun track(courier: Courier) {
        aggregateTracker.track(courier)
    }

    override fun get(courierId: UUID): Courier? = repository.findByIdOrNull(courierId)

    override fun getAvailableCouriers(): List<Courier> = repository.findAllAvailable()

    override fun getAllCouriers(): List<Courier> = repository.findAll()

}