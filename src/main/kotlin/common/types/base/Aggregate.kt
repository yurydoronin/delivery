package common.types.base

abstract class Aggregate<T : Any> protected constructor(
    id: T
) : DomainEntity<T>(id), AggregateRoot {

    protected var domainEvents: MutableList<DomainEvent>? = mutableListOf()

    override fun allDomainEvents(): List<DomainEvent> = domainEvents ?: emptyList()

    override fun addDomainEvent(event: DomainEvent) {
        domainEvents = domainEvents ?: mutableListOf()
        domainEvents!!.add(event)
    }

    override fun clearDomainEvents() {
        domainEvents?.clear()
    }
}