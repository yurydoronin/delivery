package delivery.domain.kernel

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import delivery.common.types.base.ValueObject
import delivery.common.types.error.BusinessError
import kotlin.math.abs

private const val MIN = 1
private const val MAX = 10

/**
 * Location - это координата на доске, она состоит из X (горизонталь) и Y (вертикаль)
 */
@ConsistentCopyVisibility
data class Location private constructor(
    val x: Int,
    val y: Int
) : ValueObject {

    companion object {
        fun of(x: Int, y: Int): Either<LocationError, Location> = either {

            ensure(x in MIN..MAX) { LocationError.InvalidX(x) }
            ensure(y in MIN..MAX) { LocationError.InvalidY(y) }

            Location(x = x, y = y)
        }

        fun restore(x: Int, y: Int) = Location(x = x, y = y)
    }

    /**
     * Манхэттенское расстояние: расстояние между двумя точками (x1, y1) и (x2, y2) — это сумма разностей по каждой оси.
     */
    fun distanceTo(target: Location) = abs(target.x - x) + abs(target.y - y)
}

sealed class LocationError(override val message: String) : BusinessError {
    data class InvalidX(val value: Int) : LocationError("X must be between $MIN and $MAX, actual: $value")
    data class InvalidY(val value: Int) : LocationError("Y must be between $MIN and $MAX, actual: $value")
}
