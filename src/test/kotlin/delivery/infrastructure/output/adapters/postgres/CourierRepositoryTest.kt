package delivery.infrastructure.output.adapters.postgres

import arrow.core.raise.either
import com.ninjasquad.springmockk.MockkBean
import delivery.core.application.ports.output.AggregateTracker
import delivery.core.application.ports.output.UnitOfWork
import delivery.core.domain.kernel.Location
import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.order.Order
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

class CourierRepositoryTest @Autowired constructor(
    private val unitOfWork: UnitOfWork,
    private val courierRepository: CourierRepository
) : BaseRepositoryTest() {
    // relaxed = true позволяет не писать `every { aggregateTracker.track(any()) } just runs` явно
    @MockkBean(relaxed = true)
    lateinit var aggregateTracker: AggregateTracker

    @Test
    fun `add new courier`() {
        either {
            // Arrange
            val courier = Courier.of("Новый", 3, Location.of(1, 1)).bind()

            // Act
            courierRepository.track(courier)
            every { aggregateTracker.getTracked() } returns listOf(courier)
            unitOfWork.commit()

            val newCourier = courierRepository.get(courier.id)
            newCourier shouldNotBe null
            newCourier!!.name shouldBe "Новый"
            verify { aggregateTracker.track(courier) }
        }
    }

    @Test
    fun `update existing courier`() {
        either {
            // Arrange
            val courier = Courier.of("Обновляемый", 2, Location.of(2, 2)).bind()
            courierRepository.track(courier)
            every { aggregateTracker.getTracked() } returns listOf(courier)
            unitOfWork.commit()

            val updatedLocation = Location.of(5, 5)
            courier.location = updatedLocation

            // Act
            courierRepository.track(courier)
            every { aggregateTracker.getTracked() } returns listOf(courier)
            unitOfWork.commit()

            // Assert
            val updated = courierRepository.get(courier.id)
            updated!!.location shouldBe updatedLocation
            verify { aggregateTracker.track(courier) }
        }
    }

    @Test
    fun `get courier`() {
        either {
            // Arrange
            val courier = Courier.of("Иван", 3, Location.of(1, 1)).bind()
            courierRepository.track(courier)
            every { aggregateTracker.getTracked() } returns listOf(courier)
            unitOfWork.commit()

            // Act
            val found = courierRepository.get(courier.id)

            // Assert
            found shouldNotBe null
            found!!.id shouldBe courier.id
            found.name shouldBe courier.name
            found.speed shouldBe courier.speed
        }
    }

    @Test
    fun `get available couriers`() {
        either {
            // Arrange
            val free1 = Courier.of("Свободный-1", 2, Location.of(1, 1)).bind()
            val free2 = Courier.of("Свободный-2", 2, Location.of(2, 2)).bind()
            val order = Order.of(UUID.randomUUID(), Location.of(5, 5), 5)
            val busy = Courier.of("Занятой", 2, Location.of(3, 3)).bind()
            busy.takeOrder(order)

            courierRepository.track(free1)
            courierRepository.track(free2)
            courierRepository.track(busy)
            every { aggregateTracker.getTracked() } returns listOf(free1, free2, busy)
            unitOfWork.commit()

            // Act
            val available = courierRepository.getAvailableCouriers()

            // Assert
            available.shouldHaveSize(2)
            available.map { it.name }.shouldContainAll("Свободный-1", "Свободный-2")
            available.map { it.id }.shouldNotContain(busy.id)
            verify { aggregateTracker.track(free1) }
            verify { aggregateTracker.track(free2) }
            verify { aggregateTracker.track(busy) }
        }
    }
}