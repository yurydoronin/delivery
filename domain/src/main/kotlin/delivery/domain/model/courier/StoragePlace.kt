package delivery.domain.model.courier

import arrow.core.Either
import arrow.core.raise.either
import com.github.f4b6a3.uuid.UuidCreator
import delivery.common.types.base.DomainEntity
import delivery.common.types.error.BusinessError
import java.util.UUID
import kotlin.enums.enumEntries

/**
 * Место хранения заказа (рюкзак, багажник курьера)
 */
class StoragePlace private constructor(
    id: UUID,
    val name: StoragePlaceName,
    /**
     * Допустимый объем места хранения
     */
    val totalVolume: Int,
) : DomainEntity<UUID>(id) {

    private var _orderId: UUID? = null
    val orderId: UUID?
        get() = _orderId

    companion object {
        fun of(name: StoragePlaceName, totalVolume: Int): StoragePlace {
            require(totalVolume > 0) { "Total volume must be greater than 0" }
            return StoragePlace(id = UuidCreator.getTimeOrderedEpoch(), name, totalVolume)
        }

        fun restore(
            id: UUID,
            name: StoragePlaceName,
            totalVolume: Int,
            orderId: UUID?,
        ) = StoragePlace(id, name, totalVolume).apply {
            _orderId = orderId
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

    fun store(orderId: UUID, orderVolume: Int): Either<StorageError, Unit> = either {
        when (canStore(orderVolume)) {
            is StorageCheck.Ok -> _orderId = orderId
            is StorageCheck.Occupied -> raise(StorageError.Occupied)
            is StorageCheck.NotEnoughSpace -> raise(StorageError.NotEnoughSpace)
        }
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
    BACKPACK("Сумка"),
    BICYCLE_BACKPACK("Вело-Сумка"),
    BICYCLE_TRUNK("Вело-Багажник"),
    CAR_BACKPACK("Авто-Сумка"),
    CAR_TRUNK("Авто-Багажник"),
    CAR_TRAILER("Авто-Прицеп");

    companion object {
        fun fromName(name: String): Either<StorageError, StoragePlaceName> = either {
            entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) }
                ?: raise(StorageError.UnknownStoragePlace(name))
        }

        /**
         * Используется при восстановлении из БД.
         * Некорректное значение означает нарушение целостности данных.
         */
        fun restore(name: String): StoragePlaceName =
            enumEntries<StoragePlaceName>()
                .firstOrNull { it.name == name }
                ?: error("Unknown storage place in database: $name")
    }
}

sealed class StorageCheck {
    object Ok : StorageCheck()
    object Occupied : StorageCheck()
    object NotEnoughSpace : StorageCheck()
}

sealed class StorageError(override val message: String) : BusinessError {
    data object Occupied : StorageError("Storage is already occupied")
    data object NotEnoughSpace : StorageError("Order volume exceeds storage capacity")
    data class UnknownStoragePlace(val name: String) : StorageError("Unknown storage place: $name")
}
