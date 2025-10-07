package delivery.infrastructure.output.adapters.postgres

import delivery.core.application.ports.output.AggregateTracker
import delivery.core.application.ports.output.UnitOfWork
import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.order.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UnitOfWorkImpl(
    val tracker: AggregateTracker,
    val courierRepository: CourierJpaRepository,
    val orderRepository: OrderJpaRepository
) : UnitOfWork {

    @Transactional
    override fun commit() {
        try {
            tracker.getTracked().forEach { aggregate ->
                when (aggregate) {
                    is Courier -> courierRepository.save(aggregate)
                    is Order -> orderRepository.save(aggregate)
                }
            }
        } finally {
            tracker.clear()
        }
    }
}