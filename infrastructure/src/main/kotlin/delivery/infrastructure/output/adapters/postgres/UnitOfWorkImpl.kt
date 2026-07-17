package delivery.infrastructure.output.adapters.postgres

import com.google.protobuf.util.JsonFormat
import delivery.application.ports.output.AggregateTracker
import delivery.application.ports.output.DomainEventOutboxPort
import delivery.application.ports.output.UnitOfWork
import delivery.application.ports.output.outbox.IntegrationOutboxPort
import delivery.application.ports.output.outbox.OutboxMessage
import delivery.domain.model.courier.Courier
import delivery.domain.model.order.Order
import delivery.infrastructure.output.adapters.postgres.outbox.integrationEventType
import delivery.infrastructure.output.adapters.postgres.outbox.isIntegrationEvent
import delivery.infrastructure.output.adapters.postgres.outbox.toIntegrationEventPayload
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Component
@Scope(SCOPE_PROTOTYPE)
class UnitOfWorkImpl(
    private val tracker: AggregateTracker,
    private val courierRepo: JdbcCourierRepository,
    private val orderRepo: JdbcOrderRepository,
    private val domainEventOutbox: DomainEventOutboxPort,
    private val integrationEventOutbox: IntegrationOutboxPort,
    private val objectMapper: ObjectMapper
) : UnitOfWork {

    private val log = LoggerFactory.getLogger(UnitOfWorkImpl::class.java)
    private val printer = JsonFormat.printer().alwaysPrintFieldsWithNoPresence().omittingInsignificantWhitespace()

    @Transactional
    override fun commit() {
        try {
            tracker.getTracked().forEach { aggregate ->
                when (aggregate) {
                    is Courier -> courierRepo.save(aggregate)
                    is Order -> orderRepo.save(aggregate)
                }
                // Сохраняем события в domainEventOutbox и integrationEventOutbox
                aggregate.allDomainEvents()
                    .forEach { domainEvent ->
                        domainEventOutbox.save(
                            OutboxMessage(
                                id = domainEvent.eventId,
                                eventType = domainEvent.javaClass.name,
                                aggregateId = aggregate.id,
                                aggregateType = aggregate.javaClass.simpleName,
                                payload = objectMapper.writeValueAsString(domainEvent),
                                occurredOnUtc = domainEvent.occurredOnUtc
                            )
                        )

                        if (domainEvent.isIntegrationEvent()) {
                            integrationEventOutbox.save(
                                OutboxMessage(
                                    id = domainEvent.eventId,
                                    eventType = domainEvent.integrationEventType(),
                                    aggregateId = aggregate.id,
                                    aggregateType = aggregate.javaClass.simpleName,
                                    payload = printer.print(domainEvent.toIntegrationEventPayload()),
                                    occurredOnUtc = domainEvent.occurredOnUtc,
                                )
                            )
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
