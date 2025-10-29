package delivery.infrastructure.output.adapters.postgres

import common.types.base.AggregateRoot
import delivery.core.application.ports.output.AggregateTracker
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class AggregateTrackerImpl : AggregateTracker {

    private val tracked = mutableSetOf<AggregateRoot<UUID>>()

    override fun track(aggregate: AggregateRoot<UUID>) {
        tracked.add(aggregate)
    }

    override fun getTracked(): List<AggregateRoot<UUID>> = tracked.toList()

    override fun clear() {
        tracked.clear()
    }
}