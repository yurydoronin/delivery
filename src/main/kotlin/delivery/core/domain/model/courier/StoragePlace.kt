package delivery.core.domain.model.courier

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import common.types.base.DomainEntity
import common.types.error.BusinessError
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
    val isEmpty get() = orderId == null

    /**
     * Поместить заказ в место хранения можно только, если:
     * - Объем заказа не превышает объем места хранения
     * - В месте хранения нет другого заказа
     */
    fun canStore(orderVolume: Int): StorageCheck = when {
        !isEmpty -> StorageCheck.Occupied
        orderVolume > totalVolume -> StorageCheck.NotEnoughSpace
        else -> StorageCheck.Ok
    }

    fun store(orderId: UUID, orderVolume: Int): Either<StorageError, Unit> =
        when (canStore(orderVolume)) {
            is StorageCheck.Ok -> {
                _orderId = orderId
                Unit.right()
            }
            is StorageCheck.Occupied -> StorageError.Occupied.left()
            is StorageCheck.NotEnoughSpace -> StorageError.NotEnoughSpace.left()
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

enum class StoragePlaceName(val displayName: String) {
    BACKPACK("рюкзак"),
    TRUNK("багажник"),
}

sealed class StorageCheck {
    object Ok : StorageCheck()
    object Occupied : StorageCheck()
    object NotEnoughSpace : StorageCheck()
}

sealed class StorageError(override val message: String) : BusinessError {
    data object Occupied : StorageError("Storage is already occupied")
    data object NotEnoughSpace : StorageError("Order volume exceeds storage capacity")
}
