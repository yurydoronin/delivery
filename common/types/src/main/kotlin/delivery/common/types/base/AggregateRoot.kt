package delivery.common.types.base

interface AggregateRoot<ID : Any> {
    val id: ID
    val allDomainEvents: List<DomainEvent>
    fun addDomainEvent(event: DomainEvent)
    fun clearDomainEvents()
}