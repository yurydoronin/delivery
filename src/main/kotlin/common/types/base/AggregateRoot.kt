package common.types.base

interface AggregateRoot {
    fun allDomainEvents(): List<DomainEvent>
    fun addDomainEvent(event: DomainEvent)
    fun clearDomainEvents()
}