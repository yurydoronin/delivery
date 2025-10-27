package common.types.base

interface AggregateRoot<ID : Any> {
    val id: ID
    fun allDomainEvents(): List<DomainEvent>
    fun addDomainEvent(event: DomainEvent)
    fun clearDomainEvents()
}