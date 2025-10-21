package delivery.core.application

import arrow.core.left
import arrow.core.raise.either
import delivery.core.application.ports.input.queries.AssignedCouriersError
import delivery.core.application.ports.input.queries.GetAssignedCouriersUseCaseImpl
import delivery.core.domain.kernel.Location
import delivery.core.domain.model.courier.Courier
import delivery.core.domain.model.order.Order
import delivery.infrastructure.output.adapters.postgres.BaseRepositoryTest
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import java.util.UUID
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired

class GetAssignedCouriersUseCaseImplTest @Autowired constructor(
    private val em: EntityManager,
    private val sut: GetAssignedCouriersUseCaseImpl
) : BaseRepositoryTest() {

    @Test
    fun `get assigned couriers`() {
        either {
            // Arrange
            val courier1 = Courier.of("Маша", 4, Location.of(1, 1)).bind()
            val courier2 = Courier.of("Коля", 1, Location.of(2, 2)).bind()
            em.persist(courier1)
            em.persist(courier2)

            val order = Order.of(UUID.randomUUID(), Location.of(3, 3), 1)
            order.assignToCourier(courier1.id)
            em.persist(order)
            em.flush()

            // Act
            val result = sut.execute()

            // Assert
            val couriers = result.shouldBeRight()
            couriers.size shouldBe 1
            couriers.first().name shouldBe courier1.name
            couriers.first().location shouldBe courier1.location
        }
    }

    @Test
    fun `fails when no assigned couriers`() {
        val result = sut.execute()

        result shouldBe AssignedCouriersError.NoAssignedCouriers.left()
    }
}
