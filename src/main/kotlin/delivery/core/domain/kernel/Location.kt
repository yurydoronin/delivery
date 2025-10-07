package delivery.core.domain.kernel

import common.types.base.ValueObject
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import kotlin.math.abs

/**
 * Location - это координата на доске, она состоит из X (горизонталь) и Y (вертикаль)
 */
@ConsistentCopyVisibility
@Embeddable
data class Location private constructor(
    @Column(name = "location_x", nullable = false)
    val x: Int,
    @Column(name = "location_y", nullable = false)
    val y: Int
) : ValueObject {

    companion object {
        private const val MIN = 1
        private const val MAX = 10

        fun of(x: Int, y: Int) = Location(
            x = x.also { require(it in MIN..MAX) { "X must be between $MIN and $MAX" } },
            y = y.also { require(it in MIN..MAX) { "Y must be between $MIN and $MAX" } }
        )

        fun random() = of(
            x = (MIN..MAX).random(),
            y = (MIN..MAX).random()
        )
    }

    /**
     * Расстояние между двумя точками (x1, y1) и (x2, y2) — это сумма разностей по каждой оси.
     */
    fun distanceTo(target: Location) = abs(target.x - x) + abs(target.y - y)

    /**
     * Две координаты равны, если их X и Y равны
     */
    operator fun compareTo(other: Location): Int =
        compareValuesBy(a = this, b = other, { it.x }, { it.y })
}
