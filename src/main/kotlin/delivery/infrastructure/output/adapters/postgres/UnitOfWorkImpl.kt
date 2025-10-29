package delivery.infrastructure.output.adapters.postgres

import com.fasterxml.jackson.databind.ObjectMapper
import delivery.core.application.ports.output.AggregateTracker
import delivery.core.application.ports.output.UnitOfWork
import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.order.Order
import delivery.infrastructure.output.adapters.postgres.outbox.OutboxMessage
import delivery.infrastructure.output.adapters.postgres.outbox.OutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Scope(SCOPE_PROTOTYPE)
class UnitOfWorkImpl(
    private val tracker: AggregateTracker,
    private val courierRepository: CourierJpaRepository,
    private val orderRepository: OrderJpaRepository,
    private val outboxRepository: OutboxRepository,
    private val objectMapper: ObjectMapper,
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
                // Сохраняем события в Outbox
                aggregate.allDomainEvents().forEach { domainEvent ->
                    runCatching {
                        outboxRepository.save(
                            OutboxMessage(
                                id = domainEvent.eventId,
                                eventType = domainEvent.javaClass.name,
                                aggregateId = aggregate.id,
                                aggregateType = aggregate.javaClass.simpleName,
                                payload = objectMapper.writeValueAsString(domainEvent),
                                occurredOnUtc = domainEvent.occurredOnUtc,
                            )
                        )
                    }.onFailure { e ->
                        log.error("Failed to serialize domain event for outbox", e)
                    }
                }
                aggregate.clearDomainEvents()
            }
        } catch (e: Exception) {
            log.error("UnitOfWork commit failed", e)
        } finally {
            tracker.clear()
        }
    }
}