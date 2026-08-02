package delivery.domain.model.courier

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.github.f4b6a3.uuid.UuidCreator
import delivery.common.types.base.DomainEntity
import delivery.common.types.error.BusinessError
import java.util.UUID

/**
 * Место хранения заказа (рюкзак, багажник курьера).
 *
 * @property totalVolume допустимый объем места хранения.
 */
class StoragePlace private constructor(
    id: UUID,
    val type: StoragePlaceType,
    val totalVolume: Int,
) : DomainEntity<UUID>(id) {

    var orderId: UUID? = null
        private set

    companion object {
        fun of(type: StoragePlaceType): Either<StorageError, StoragePlace> = either {

            ensure(type.volume > 0) { StorageError.InvalidVolume }

            StoragePlace(
                id = UuidCreator.getTimeOrderedEpoch(),
                type = type,
                totalVolume = type.volume,
            )
        }

        fun restore(
            id: UUID,
            type: StoragePlaceType,
            totalVolume: Int,
            orderId: UUID?,
        ) = StoragePlace(
            id = id,
            type = type,
            totalVolume = totalVolume
        ).apply {
            this.orderId = orderId
        }
    }

    /**
     * Место хранения считается пустым, если OrderId не установлен.
     */
    private val isEmpty get() = orderId == null

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
            is StorageCheck.Ok -> this@StoragePlace.orderId = orderId
            is StorageCheck.Occupied -> raise(StorageError.Occupied)
            is StorageCheck.NotEnoughSpace -> raise(StorageError.NotEnoughSpace)
        }
    }

    /**
     * Извлечение заказа из места хранения
     */
    fun clear(): UUID? {
        val extracted = orderId
        orderId = null
        return extracted
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
    data object InvalidVolume : StorageError("Total volume must be positive")
}
