package delivery.core.domain.kernel

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LocationTest {

    @Test
    fun `create Location with valid coordinates`() {
        val location = Location.of(1, 1)

        assertEquals(1, location.x)
        assertEquals(1, location.y)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 5, X must be between 1 and 10",
        "11, 5, X must be between 1 and 10",
        "5, 0, Y must be between 1 and 10",
        "5, 12, Y must be between 1 and 10"
    )
    fun `throw an exception with invalid coordinates`(x: Int, y: Int, expectedMessage: String) {
        val exception = assertFailsWith<IllegalArgumentException> {
            Location.of(x, y)
        }
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun `calculate Manhattan distance correctly`() {
        val a = Location.of(4, 9)
        val b = Location.of(2, 6)

        assertEquals(5, a.distanceTo(b))
    }

    @Test
    fun `compareTo respects x and y order`() {
        val a = Location.of(1, 1)
        val b = Location.of(2, 1)
        val c = Location.of(2, 2)

        assertTrue(a < b)
        assertTrue(b < c)
        assertTrue(c > a)
        assertEquals(0, b.compareTo(Location.of(2, 1)))
    }

}