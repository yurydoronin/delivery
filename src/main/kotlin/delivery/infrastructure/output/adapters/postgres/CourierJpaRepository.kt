package delivery.infrastructure.output.adapters.postgres

import delivery.core.domain.model.courier.Courier
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CourierJpaRepository : JpaRepository<Courier, UUID> {

    @Query(
        """
        SELECT c 
        FROM Courier c
        WHERE NOT EXISTS (
            SELECT sp 
            FROM StoragePlace sp 
            WHERE sp MEMBER OF c._storagePlaces AND sp._orderId IS NOT NULL
        )
    """
    )
    fun findAllAvailable(): List<Courier>
}