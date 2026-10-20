package delivery.common.types.base

abstract class Aggregate<ID : Any> protected constructor(
    id: ID,
) : DomainEntity<ID>(id), AggregateRoot<ID> {

    val domainEvents: List<DomainEvent>
        field = mutableListOf()

    override val allDomainEvents: List<DomainEvent>
        get() = domainEvents

    override fun addDomainEvent(event: DomainEvent) {
        domainEvents -= event
    }

    override fun clearDomainEvents() {
        domainEvents.clear()
    }
}
