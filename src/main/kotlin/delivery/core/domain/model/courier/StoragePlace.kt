package delivery.core.domain.model.courier

import common.types.base.DomainEntity
import jakarta.persistence.*
import java.util.UUID

/**
 * Место хранения заказа (рюкзак, багажник курьера)
 */
@Entity
@Table(name = "storage_places")
class StoragePlace private constructor(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val name: StoragePlaceName,
    /**
     * Допустимый объем места хранения
     */
    @Column(name = "total_volume", nullable = false)
    val totalVolume: Int,
) : DomainEntity<UUID>(UUID.randomUUID()) {

    @Column(name = "order_id")
    private var _orderId: UUID? = null

    val orderId: UUID?
        get() = _orderId

    companion object {
        fun of(name: StoragePlaceName, totalVolume: Int): StoragePlace {
            require(totalVolume > 0) { "Total volume must be greater than 0" }
            return StoragePlace(name, totalVolume)
        }
    }

    /**
     * Место хранения считается пустым, если OrderId не установлен.
     */
    private val isEmpty: Boolean
        get() = _orderId == null

    /**
     * Поместить заказ в место хранения можно только, если:
     * - Объем заказа не превышает объем места хранения
     * - В месте хранения нет другого заказа
     */
    fun canStore(orderVolume: Int) = isEmpty && (orderVolume <= totalVolume)

    fun store(orderId: UUID, orderVolume: Int) {
        require(canStore(orderVolume)) {
            "Cannot put order: either storage is not empty or volume exceeded"
        }
        _orderId = orderId
    }

    /**
     * Извлечение заказа из места хранения
     */
    fun clear(): UUID? {
        val extracted = _orderId
        _orderId = null
        return extracted
    }
}

/**
 * Название места хранения
 */
enum class StoragePlaceName(val displayName: String) {
    BACKPACK("рюкзак"),
    TRUNK("багажник")
}

