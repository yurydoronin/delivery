package delivery.infrastructure.output.adapters.postgres

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
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Transactional
class CourierRepositoryTest @Autowired constructor(
    private val unitOfWork: UnitOfWork,
    private val courierRepository: CourierRepository
) {
    // relaxed = true позволяет не писать `every { aggregateTracker.track(any()) } just runs` явно
    @MockkBean(relaxed = true)
    lateinit var aggregateTracker: AggregateTracker

    @Test
    fun `save courier`() {
        // Arrange
        val courier = Courier.of("Сохраняемый", 1, Location.of(1, 1))

        // Act
        courierRepository.save(courier)
        unitOfWork.commit()

        // Assert
        val saved = courierRepository.get(courier.id)
        saved shouldNotBe null
        saved!!.name shouldBe "Сохраняемый"
        verify { aggregateTracker.track(courier) }
    }


    @Test
    fun `add new courier`() {
        // Arrange
        val courier = Courier.of("Новый", 3, Location.of(1, 1))

        // Act
        courierRepository.add(courier)
        unitOfWork.commit()

        val newCourier = courierRepository.get(courier.id)
        newCourier shouldNotBe null
        newCourier!!.name shouldBe "Новый"
        verify { aggregateTracker.track(courier) }
    }

    @Test
    fun `update existing courier`() {
        // Arrange
        val courier = Courier.of("Обновляемый", 2, Location.of(2, 2))
        courierRepository.add(courier)
        unitOfWork.commit()

        val updatedLocation = Location.of(5, 5)
        courier.location = updatedLocation

        // Act
        courierRepository.update(courier)
        unitOfWork.commit()

        // Assert
        val updated = courierRepository.get(courier.id)
        updated!!.location shouldBe updatedLocation
        verify { aggregateTracker.track(courier) }
    }

    @Test
    fun `get courier`() {
        // Arrange
        val courier = Courier.of("Иван", 3, Location.of(1, 1))
        courierRepository.save(courier)
        unitOfWork.commit()

        // Act
        val found = courierRepository.get(courier.id)

        // Assert
        found shouldNotBe null
        found!!.id shouldBe courier.id
        found.name shouldBe courier.name
        found.speed shouldBe courier.speed
    }

    @Test
    fun `get available couriers`() {
        // Arrange
        val free1 = Courier.of("Свободный-1", 2, Location.of(1, 1))
        val free2 = Courier.of("Свободный-2", 2, Location.of(2, 2))
        val order = Order.of(UUID.randomUUID(), Location.of(5, 5), 5)
        val busy = Courier.of("Занятой", 2, Location.of(3, 3))
        busy.takeOrder(order)

        courierRepository.add(free1)
        courierRepository.add(free2)
        courierRepository.add(busy)
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