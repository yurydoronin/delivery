package delivery.core.domain.model.courier

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import common.types.base.Aggregate
import common.types.error.BusinessError
import delivery.core.domain.kernel.Location
import delivery.core.domain.model.order.Order
import jakarta.persistence.*
import java.util.UUID
import kotlin.math.abs

@Entity
@Table(name = "couriers")
class Courier private constructor(
    @Column(nullable = false)
    val name: String,
    /**
     * Скорость измеряется количеством клеток, которые курьер может пройти за один шаг.
     * Скорость курьера зависит от наличия/отсутствия транспорта.
     */
    @Column(nullable = false)
    val speed: Int,
    @Embedded
    var location: Location,
) : Aggregate<UUID>(UUID.randomUUID()) {

    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @JoinColumn(name = "courier_id")
    private val _storagePlaces = mutableListOf<StoragePlace>()
    val storagePlaces: List<StoragePlace>
        get() = _storagePlaces.toList()

    companion object {
        // Каждый курьер владеет местом хранения "Сумка" объемом 10 литров
        private const val VOLUME = 10

        fun of(name: String, speed: Int, location: Location): Courier {
            require(name.isNotBlank()) { "Name must not be blank" }
            require(speed > 0) { "Speed must be positive" }

            val courier = Courier(name, speed, location)
            courier.addStoragePlace(StoragePlaceName.BACKPACK, VOLUME)
            return courier
        }
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

    fun completeOrder(order: Order): Either<CourierError, Unit> =
        _storagePlaces.firstOrNull { it.orderId == order.id }
            ?.also { it.clear() }
            ?.let { Unit.right() } ?: CourierError.OrderNotFound.left()

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
