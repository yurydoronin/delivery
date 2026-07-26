package delivery.infrastructure

import com.ninjasquad.springmockk.MockkBean
import delivery.BaseRepositoryTest
import delivery.application.ports.output.AggregateTracker
import delivery.application.ports.output.UnitOfWork
import delivery.domain.kernel.Location
import delivery.domain.kernel.LocationTestData
import delivery.domain.model.courier.Courier
import delivery.domain.model.order.Order
import delivery.infrastructure.output.adapters.postgres.JdbcCourierRepository
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired

class JdbcCourierRepositoryTest @Autowired constructor(
    private val unitOfWork: UnitOfWork,
    private val jdbcCourierRepository: JdbcCourierRepository
) : BaseRepositoryTest() {
    // relaxed = true позволяет не писать `every { aggregateTracker.track(any()) } just runs` явно
    @MockkBean(relaxed = true)
    lateinit var aggregateTracker: AggregateTracker

    @Test
    fun `add new courier`() {
        // Arrange
        val courier = Courier.of("Новый", 3, LocationTestData.random()).shouldBeRight()

        // Act
        jdbcCourierRepository.track(courier)
        every { aggregateTracker.getTracked() } returns listOf(courier)
        unitOfWork.commit()

        val newCourier = jdbcCourierRepository.get(courier.id)
        newCourier shouldNotBe null
        newCourier!!.name shouldBe "Новый"
        verify { aggregateTracker.track(courier) }
    }

    @Test
    fun `update existing courier`() {
        // Arrange
        val courier = Courier.of("Обновляемый", 2, LocationTestData.random()).shouldBeRight()
        jdbcCourierRepository.track(courier)
        every { aggregateTracker.getTracked() } returns listOf(courier)
        unitOfWork.commit()

        val updatedLocation = LocationTestData.random()
        courier.location = updatedLocation

        // Act
        jdbcCourierRepository.track(courier)
        every { aggregateTracker.getTracked() } returns listOf(courier)
        unitOfWork.commit()

        // Assert
        val updated = jdbcCourierRepository.get(courier.id)
        updated!!.location shouldBe updatedLocation
        verify { aggregateTracker.track(courier) }
    }

    @Test
    fun `get courier`() {
        // Arrange
        val courier = Courier.of("Иван", 3, LocationTestData.random()).shouldBeRight()
        jdbcCourierRepository.track(courier)
        every { aggregateTracker.getTracked() } returns listOf(courier)
        unitOfWork.commit()

        // Act
        val found = jdbcCourierRepository.get(courier.id)

        // Assert
        found shouldNotBe null
        found!!.id shouldBe courier.id
        found.name shouldBe courier.name
        found.speed shouldBe courier.speed
    }

    @Test
    fun `get available couriers`() {
        // Arrange
        val free1 = Courier.of("Свободный-1", 2, Location.restore(1, 1)).shouldBeRight()
        val free2 = Courier.of("Свободный-2", 2, Location.restore(2, 2)).shouldBeRight()
        val order = Order.of(UUID.randomUUID(), Location.restore(5, 5), 5).shouldBeRight()
        val busy = Courier.of("Занятой", 2, Location.restore(3, 3)).shouldBeRight()
        busy.takeOrder(order)

        jdbcCourierRepository.track(free1)
        jdbcCourierRepository.track(free2)
        jdbcCourierRepository.track(busy)
        every { aggregateTracker.getTracked() } returns listOf(free1, free2, busy)
        unitOfWork.commit()

        // Act
        val available = jdbcCourierRepository.findCouriersWithAnyFreeStorage()

        // Assert
        available.shouldHaveSize(2)
        available.map { it.name }.shouldContainAll("Свободный-1", "Свободный-2")
        available.map { it.id }.shouldNotContain(busy.id)
        verify { aggregateTracker.track(free1) }
        verify { aggregateTracker.track(free2) }
        verify { aggregateTracker.track(busy) }
    }
}
