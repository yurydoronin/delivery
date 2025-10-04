package delivery.infrastructure.output.adapters.postgres

import delivery.core.application.ports.output.AggregateTracker
import delivery.core.application.ports.output.CourierRepositoryPort
import delivery.core.domain.model.courier.Courier
import java.util.UUID
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class CourierRepository(
    val aggregateTracker: AggregateTracker,
    val repository: CourierJpaRepository
) : CourierRepositoryPort {

    override fun add(courier: Courier) {
        require(!repository.existsById(courier.id)) {
            "Courier with id ${courier.id} already exists"
        }
        aggregateTracker.track(courier)
    }

    override fun update(courier: Courier) {
        require(repository.existsById(courier.id)) {
            "Cannot update non-existent courier with id ${courier.id}"
        }
        aggregateTracker.track(courier)
    }

    override fun get(courierId: UUID): Courier? = repository.findByIdOrNull(courierId)

    override fun getAvailableCouriers(): List<Courier> = repository.findAllAvailable()

}