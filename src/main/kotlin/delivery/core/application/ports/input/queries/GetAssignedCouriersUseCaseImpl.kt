package delivery.core.application.ports.input.queries

import arrow.core.Either
import arrow.core.raise.either
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
    override fun execute(): Either<BusinessError, List<GetAssignedCouriersResult>> = either {
        val sql = """
            SELECT c.id, c.name, c.location_x, c.location_y
            FROM couriers c
            WHERE EXISTS (
                SELECT 1 FROM orders o
                WHERE o.courier_id = c.id
                  AND o.status = 'ASSIGNED'
            )
        """.trimIndent()

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

        if (results.isEmpty()) raise(AssignedCouriersError.NoAssignedCouriers)

        results
    }
}

sealed class AssignedCouriersError(override val message: String) : BusinessError {
    data object NoAssignedCouriers : AssignedCouriersError("No assigned couriers found")
}
