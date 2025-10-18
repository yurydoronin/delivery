package delivery.infrastructure.output.adapters.postgres

import common.types.base.AggregateRoot
import delivery.core.application.ports.output.AggregateTracker
import org.springframework.stereotype.Component

@Component
class AggregateTrackerImpl : AggregateTracker {

    private val tracked = mutableSetOf<AggregateRoot>()

    override fun track(aggregate: AggregateRoot) {
        tracked.add(aggregate)
    }

    override fun getTracked(): List<AggregateRoot> = tracked.toList()

    override fun clear() {
        tracked.clear()
    }
}