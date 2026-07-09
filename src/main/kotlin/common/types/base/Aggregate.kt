package common.types.base

abstract class Aggregate<ID : Any> protected constructor(
    id: ID,
    private var _version: Long = 0
) : DomainEntity<ID>(id), AggregateRoot<ID> {

    val version: Long
        get() = _version

    internal fun incrementVersion() {
        _version++
    }

    private val _domainEvents: MutableList<DomainEvent> = mutableListOf()

    override fun allDomainEvents(): List<DomainEvent> = _domainEvents

    override fun addDomainEvent(event: DomainEvent) {
        _domainEvents.add(event)
    }

    override fun clearDomainEvents() {
        _domainEvents.clear()
    }
}
