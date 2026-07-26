package delivery.domain.kernel

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class LocationTest {

    @Test
    fun `create Location with valid coordinates`() {
        val location = Location.of(1, 1).shouldBeRight()

        location.x shouldBeExactly 1
        location.y shouldBeExactly 1
    }

    @ParameterizedTest
    @CsvSource(
        "0, 5, 'X must be between 1 and 10, actual: 0'",
        "11, 5, 'X must be between 1 and 10, actual: 11'",
        "5, 0, 'Y must be between 1 and 10, actual: 0'",
        "5, 12, 'Y must be between 1 and 10, actual: 12'",
    )
    fun `raise business error with invalid coordinates`(x: Int, y: Int, expectedMessage: String) {
        val result = Location.of(x, y).shouldBeLeft()

        result.message shouldBe expectedMessage
    }

    @Test
    fun `calculate Manhattan distance correctly`() {
        val a = Location.of(4, 9).shouldBeRight()
        val b = Location.of(2, 6).shouldBeRight()

        a.distanceTo(b) shouldBeExactly 5
    }
}
