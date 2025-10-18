package delivery.core.application.ports.input.queries

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import common.types.error.BusinessError
import delivery.core.domain.kernel.Location
import java.util.UUID
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetAssignedCouriersUseCaseImpl(
    private val jdbcTemplate: JdbcTemplate
) : GetAssignedCouriersUseCase {

    @Transactional(readOnly = true)
    override fun execute(): Either<BusinessError, List<GetAssignedCouriersResult>> {
        val sql = """
            SELECT c.id, c.name, c.location_x, c.location_y
            FROM couriers c
            WHERE EXISTS (
                SELECT 1 FROM orders o
                WHERE o.courier_id = c.id
                  AND o.status = 'ASSIGNED'
            )
        """

        val results = jdbcTemplate.query(sql) { rs, _ ->
            GetAssignedCouriersResult(
                courierId = UUID.fromString(rs.getString("id")),
                name = rs.getString("name"),
                location = Location.of(
                    rs.getInt("location_x"),
                    rs.getInt("location_y")
                )
            )
        }

        return results.takeIf { it.isNotEmpty() }
            ?.right()
            ?: AssignedCouriersError.NoAssignedCouriers.left()
    }
}

sealed class AssignedCouriersError(override val message: String) : BusinessError {
    data object NoAssignedCouriers : AssignedCouriersError("No assigned couriers found")
}
