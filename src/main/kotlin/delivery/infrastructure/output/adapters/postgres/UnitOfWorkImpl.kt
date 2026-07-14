package delivery.infrastructure.output.adapters.postgres

import com.google.protobuf.util.JsonFormat
import delivery.core.application.ports.output.AggregateTracker
import delivery.core.application.ports.output.UnitOfWork
import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.order.Order
import delivery.infrastructure.output.adapters.postgres.outbox.OutboxMessage
import delivery.infrastructure.output.adapters.postgres.outbox.OutboxRepository
import delivery.infrastructure.output.adapters.postgres.outbox.integrationEventType
import delivery.infrastructure.output.adapters.postgres.outbox.isIntegrationEvent
import delivery.infrastructure.output.adapters.postgres.outbox.toIntegrationEventPayload
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Scope(SCOPE_PROTOTYPE)
class UnitOfWorkImpl(
    private val tracker: AggregateTracker,
    private val jdbcCourierRepository: JdbcCourierRepository,
    private val jdbcOrderRepository: JdbcOrderRepository,
    private val outboxRepository: OutboxRepository,
) : UnitOfWork {

    private val log = LoggerFactory.getLogger(UnitOfWorkImpl::class.java)
    private val printer = JsonFormat.printer().alwaysPrintFieldsWithNoPresence().omittingInsignificantWhitespace()

    @Transactional
    override fun commit() {
        try {
            tracker.getTracked().forEach { aggregate ->
                when (aggregate) {
                    is Courier -> jdbcCourierRepository.save(aggregate)
                    is Order -> jdbcOrderRepository.save(aggregate)
                }
                // Сохраняем события в Outbox
                aggregate.allDomainEvents()
                    .filter { it.isIntegrationEvent() }
                    .forEach { domainEvent ->
                        runCatching {
                            outboxRepository.save(
                                OutboxMessage(
                                    id = domainEvent.eventId,
                                    eventType = domainEvent.integrationEventType(),
                                    aggregateId = aggregate.id,
                                    aggregateType = aggregate.javaClass.simpleName,
                                    payload = printer.print(domainEvent.toIntegrationEventPayload()),
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
            throw e
        } finally {
            tracker.clear()
        }
    }
}
