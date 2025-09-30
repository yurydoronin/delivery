package delivery.infrastructure.output.adapters.postgres

import delivery.core.domain.model.order.Order
import delivery.core.domain.model.order.OrderStatus
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface OrderJpaRepository : JpaRepository<Order, UUID> {

    fun findFirstByStatus(status: OrderStatus): Order?
    fun findAllByStatus(status: OrderStatus): List<Order>
}