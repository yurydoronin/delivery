package common.types.base

abstract class Aggregate<ID : Any> protected constructor(
    id: ID
) : DomainEntity<ID>(id), AggregateRoot<ID> {

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