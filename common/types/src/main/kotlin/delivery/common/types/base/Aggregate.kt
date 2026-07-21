package delivery.common.types.base

abstract class Aggregate<ID : Any> protected constructor(
    id: ID,
) : DomainEntity<ID>(id), AggregateRoot<ID> {

    private val _domainEvents: MutableList<DomainEvent> = mutableListOf()

    override fun allDomainEvents(): List<DomainEvent> = _domainEvents.toList()

    override fun addDomainEvent(event: DomainEvent) {
        _domainEvents.add(event)
    }

    override fun clearDomainEvents() {
        _domainEvents.clear()
    }
}
