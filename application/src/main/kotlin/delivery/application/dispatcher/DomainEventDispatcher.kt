package delivery.application.dispatcher

import delivery.application.ports.output.DomainEventOutboxPort
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DomainEventDispatcher(
    private val repository: DomainEventOutboxPort,
    private val processor: DomainEventProcessor,
) {
    private val log = LoggerFactory.getLogger(DomainEventDispatcher::class.java)

    @Scheduled(fixedDelay = 500)
    fun dispatch() {
        repository.findUnprocessedMessages()
            .takeIf { it.isNotEmpty() }
            ?.forEach { message ->
                processor.process(message)
                    .onLeft { error ->
                        log.warn("Domain event processing failed. id=${message.id}, type=${message.eventType}, reason=$error")
                    }
            } ?: log.debug("No unprocessed outbox messages")
    }
}
