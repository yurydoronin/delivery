package delivery.infrastructure.output.adapters.postgres

import delivery.core.application.ports.output.AggregateTracker
import delivery.core.application.ports.output.UnitOfWork
import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.order.Order
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UnitOfWorkImpl(
    private val tracker: AggregateTracker,
    private val courierRepository: CourierJpaRepository,
    private val orderRepository: OrderJpaRepository
) : UnitOfWork {

    private val log = LoggerFactory.getLogger(UnitOfWorkImpl::class.java)

    @Transactional
    override fun commit() {
        try {
            tracker.getTracked().forEach { aggregate ->
                when (aggregate) {
                    is Courier -> courierRepository.save(aggregate)
                    is Order -> orderRepository.save(aggregate)
                }
            }
        } catch (ex: Exception) {
            log.error("UnitOfWork commit failed: ${ex.message}", ex)
        } finally {
            tracker.clear()
        }
    }
}