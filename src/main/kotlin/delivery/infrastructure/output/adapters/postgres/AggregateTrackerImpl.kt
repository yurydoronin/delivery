package delivery.infrastructure.output.adapters.postgres

import common.types.base.AggregateRoot
import delivery.core.application.ports.output.AggregateTracker
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
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