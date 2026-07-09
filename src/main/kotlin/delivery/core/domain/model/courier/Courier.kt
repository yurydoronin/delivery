package delivery.core.domain.model.courier

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.github.f4b6a3.uuid.UuidCreator
import common.types.base.Aggregate
import common.types.error.BusinessError
import delivery.core.domain.kernel.Location
import delivery.core.domain.model.order.Order
import java.util.UUID
import kotlin.math.abs

class Courier private constructor(
    id: UUID,
    version: Long,
    val name: String,
    /**
     * Скорость измеряется количеством клеток, которые курьер может пройти за один шаг.
     * Скорость курьера зависит от наличия/отсутствия транспорта.
     */
    val speed: Int,
    var location: Location,
) : Aggregate<UUID>(id, version) {

    private var _storagePlaces = mutableListOf<StoragePlace>()
    val storagePlaces: List<StoragePlace>
        get() = _storagePlaces.toList()

    companion object {
        // Каждый курьер владеет местом хранения "Сумка" объемом 10 литров
        private const val VOLUME = 10

        fun of(
            name: String,
            speed: Int,
            location: Location,
            storageName: String = "Сумка"
        ): Either<StorageError, Courier> =
            either {
                require(name.isNotBlank()) { "Name must not be blank" }
                require(speed > 0) { "Speed must be positive" }

                val courier = Courier(
                    id = UuidCreator.getTimeOrderedEpoch(),
                    version = 0,
                    name = name,
                    speed = speed,
                    location = location,
                )
                val storagePlace = StoragePlaceName.fromName(storageName).bind()
                courier.addStoragePlace(storagePlace, VOLUME)
                courier
            }

        fun restore(
            id: UUID,
            version: Long,
            name: String,
            speed: Int,
            location: Location,
        ) = Courier(
            id = id,
            version = version,
            name = name,
            speed = speed,
            location = location,
        )
    }

    internal fun restoreStoragePlace(storagePlace: StoragePlace) {
        _storagePlaces.add(storagePlace)
    }

    fun addStoragePlace(name: StoragePlaceName, totalVolume: Int) {
        _storagePlaces.add(StoragePlace.of(name, totalVolume))
    }

    fun findAvailableStorage(order: Order): Either<CourierError, StoragePlace> =
        _storagePlaces
            .firstOrNull { it.canStore(order.volume) == StorageCheck.Ok }
            ?.right() ?: CourierError.NoAvailableStorage.left()

    /**
     * Курьер может взять заказ, если в одном из его мест хранения есть место.
     */
    fun canTakeOrder(order: Order): Boolean =
        findAvailableStorage(order).isRight()

    fun takeOrder(order: Order): Either<BusinessError, Unit> =
        findAvailableStorage(order).flatMap { place ->
            place.store(order.id, order.volume)
        }

    fun completeOrder(order: Order): Either<CourierError, Unit> = either {
        val storagePlace = _storagePlaces
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
    fun move(target: Location) {
        val difX = target.x - location.x
        val difY = target.y - location.y
        var remainingSteps = speed

        val moveX = difX.coerceIn(-remainingSteps, remainingSteps)
        remainingSteps -= abs(moveX)

        val moveY = difY.coerceIn(-remainingSteps, remainingSteps)

        location = Location.of(location.x + moveX, location.y + moveY)
    }
}

sealed class CourierError(override val message: String) : BusinessError {
    data object NoAvailableStorage : CourierError("No available storage for this order")
    data object OrderNotFound : CourierError("Order not found in any storage")
}
