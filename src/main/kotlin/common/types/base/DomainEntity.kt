package common.types.base

abstract class DomainEntity<ID : Any> protected constructor(
    val id: ID,
) {
    final override fun equals(other: Any?): Boolean {
        other ?: return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as DomainEntity<*>

        return id == other.id
    }

    final override fun hashCode(): Int = id.hashCode()
}
