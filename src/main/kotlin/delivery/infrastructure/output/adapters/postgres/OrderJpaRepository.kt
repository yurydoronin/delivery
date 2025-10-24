package delivery.infrastructure.output.adapters.postgres

import delivery.core.domain.model.order.Order
import delivery.core.domain.model.order.OrderStatus
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OrderJpaRepository : JpaRepository<Order, UUID> {

    fun findFirstBy_status(status: OrderStatus): Order?

    @Query("select o from Order o where o._status = :status")
    fun findAllByStatus(status: OrderStatus): List<Order>
}