package delivery.infrastructure.output.adapters.postgres.outbox

import delivery.application.eventhandlers.DomainEventHandlerRegistry
import delivery.application.ports.output.DomainEventOutboxPort
import delivery.common.types.base.DomainEvent
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Service
class DomainEventDispatcher(
    private val handlers: DomainEventHandlerRegistry,
    private val repository: DomainEventOutboxPort,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(DomainEventDispatcher::class.java)

    @Scheduled(fixedDelay = 500)
    @Transactional
    fun dispatch() {
        //TODO(1) пакет из 100 записей откатится целиком, если 99 прошли, а на 100 упала ошибка.
        //TODO(2) бесконечный цикл (заказ постоянно вычитывается из outbox_domain, но постоянно откатывается,
        // так как не может быть присвоен курьеру из-за недостаттка места или отсутсвия курьеров). - DLQ
        repository.findUnprocessedMessages()
            .takeIf { it.isNotEmpty() }
            ?.forEach { outboxMessage ->
                runCatching {
                    val eventClass = Class.forName(outboxMessage.eventType)
                    val eventObject = objectMapper.readValue(outboxMessage.payload, eventClass) as? DomainEvent
                        ?: throw IllegalStateException("Invalid outbox message type: $eventClass")

                    // handlers.handle(eventObject) должен бросать ошибку если заказ больше места хранения или
                    // нет свободных курьеров, чтобы откатить транзакцию
                    handlers.handle(eventObject).fold(
                        ifLeft = { error ->
                            throw RuntimeException(error.message)
                        },
                        ifRight = {}
                    )
                    outboxMessage.markAsProcessed()
                    repository.markProcessed(outboxMessage)
                }.onFailure { e ->
                    log.error("Failed to publish outbox message", e)
                }.getOrThrow() // пробрасываем исключение, чтобы выполнился rollback транзакции
            } ?: log.debug("No unprocessed outbox messages")
    }
}
