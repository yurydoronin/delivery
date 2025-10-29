package common.types.base

import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.util.ProxyUtils

@MappedSuperclass
abstract class DomainEntity<ID : Any> protected constructor(
    @Id
    open val id: ID
) {
    final override fun equals(other: Any?): Boolean {
        other ?: return false
        if (this === other) return true
        // Вытаскивает реальный класс объекта, игнорируя прокси, которые JPA/Hibernate создаёт для ленивой загрузки.
        if (javaClass != ProxyUtils.getUserClass(other)) return false

        other as DomainEntity<*>

        return id == other.id
    }

    final override fun hashCode(): Int = id.hashCode()
}
