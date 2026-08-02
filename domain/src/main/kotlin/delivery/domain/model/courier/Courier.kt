package delivery.domain.model.courier

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.github.f4b6a3.uuid.UuidCreator
import delivery.common.types.base.Aggregate
import delivery.common.types.error.BusinessError
import delivery.domain.kernel.Location
import delivery.domain.kernel.LocationError
import delivery.domain.model.order.Order
import java.util.UUID
import kotlin.math.abs

/**
 * Агрегат Курьер
 *
 * @property speed Скорость измеряется количеством клеток, которые курьер может пройти за один шаг.
 * Скорость курьера зависит от наличия/отсутствия транспорта.
 */
class Courier private constructor(
    id: UUID,
    val type: CourierType,
    val speed: Int,
    var location: Location,
) : Aggregate<UUID>(id) {

    val storagePlaces: List<StoragePlace>
        field = mutableListOf()

    companion object {
        fun of(
            type: CourierType,
            speed: Int,
            location: Location,
        ): Either<BusinessError, Courier> = either {

            ensure(speed > 0) { CourierError.InvalidSpeed }

            val courier = Courier(
                id = UuidCreator.getTimeOrderedEpoch(),
                type = type,
                speed = speed,
                location = location,
            )

            CourierStorageFactory
                .create(type)
                .forEach {
                    courier.addStoragePlace(it).bind()
                }

            courier
        }

        fun restore(
            id: UUID,
            type: CourierType,
            speed: Int,
            location: Location,
        ) = Courier(
            id = id,
            type = type,
            speed = speed,
            location = location,
        )
    }

    /**
     * Используется репозиторием при восстановлении агрегата из БД
     */
    fun restoreStoragePlace(storagePlace: StoragePlace) {
        storagePlaces += storagePlace
    }

    fun addStoragePlace(type: StoragePlaceType): Either<StorageError, Unit> = either {
        storagePlaces += StoragePlace.of(type).bind()
    }

    fun findAvailableStorage(order: Order): Either<CourierError, StoragePlace> = either {
        storagePlaces
            .firstOrNull { it.canStore(order.volume) == StorageCheck.Ok }
            ?: raise(CourierError.NoAvailableStorage)
    }

    /**
     * Курьер может взять заказ, если в одном из его мест хранения есть место.
     */
    fun canTakeOrder(order: Order): Boolean =
        storagePlaces.any {
            it.canStore(order.volume) == StorageCheck.Ok
        }

    fun takeOrder(order: Order): Either<BusinessError, Unit> =
        findAvailableStorage(order).flatMap { sp ->
            sp.store(order.id, order.volume)
        }

    fun delivered(order: Order): Either<CourierError, Unit> = either {
        val storagePlace = storagePlaces
            .firstOrNull { it.orderId == order.id }
            ?: raise(CourierError.OrderNotFound)

        storagePlace.clear()
    }

    /**
     * Возвращает количество шагов (тактов), необходимое для доставки до цели
     */
    fun calculateTimeToLocation(target: Location): Int {
        val distance = location.distanceTo(target)
        return (distance + speed - 1) / speed // округление вверх
    }

    /**
     * Перемещает курьера к указанной точке `target` с учётом его скорости
     */
    fun move(target: Location): Either<LocationError, Unit> = either {
        val difX = target.x - location.x
        val difY = target.y - location.y
        var remainingSteps = speed

        val moveX = difX.coerceIn(-remainingSteps, remainingSteps)
        remainingSteps -= abs(moveX)

        val moveY = difY.coerceIn(-remainingSteps, remainingSteps)

        location = Location.of(location.x + moveX, location.y + moveY).bind()
    }
}

sealed class CourierError(override val message: String) : BusinessError {
    data object NoAvailableStorage : CourierError("No available storage for this order")
    data object OrderNotFound : CourierError("Order not found in any storage")
    data object InvalidSpeed : CourierError("Speed must be positive")
}
