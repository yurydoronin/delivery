package common.types.base

import jakarta.persistence.MappedSuperclass

@MappedSuperclass
abstract class Aggregate<T : Any> protected constructor(
    id: T
) : DomainEntity<T>(id), AggregateRoot