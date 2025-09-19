package common.types.base

import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
abstract class DomainEntity<T : Any> protected constructor(
    @Id
    open val id: T
) {
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DomainEntity<*>) return false
        return id == other.id
    }

    final override fun hashCode(): Int = id.hashCode()
}
