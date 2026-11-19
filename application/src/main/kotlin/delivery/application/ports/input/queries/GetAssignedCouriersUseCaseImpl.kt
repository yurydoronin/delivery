package delivery.application.ports.input.queries

import arrow.core.Either
import arrow.core.raise.either
import delivery.application.dto.CourierTypeResult
import delivery.application.dto.LocationResult
import delivery.common.types.error.BusinessError
import delivery.domain.kernel.Location
import delivery.domain.model.order.OrderStatus
import java.sql.ResultSet
import java.util.UUID
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetAssignedCouriersUseCaseImpl(
    private val jdbcClient: JdbcClient,
) : GetAssignedCouriersUseCase {

    @Transactional(readOnly = true)
    override fun execute(): Either<BusinessError, List<GetAssignedCouriersResult>> = either {
        val results = jdbcClient.sql(
            """
            SELECT c.id, c.type, c.location_x, c.location_y
            FROM couriers c
            WHERE EXISTS (
                SELECT 1
                FROM orders o
                WHERE o.courier_id = c.id
                  AND o.status = :status
            )
            """.trimIndent()
        )
            .param("status", OrderStatus.ASSIGNED.name)
            .query(::toGetAssignedCouriersResult)
            .list()

        if (results.isEmpty()) raise(AssignedCouriersError.NoAssignedCouriers)

        results
    }

    private fun toGetAssignedCouriersResult(rs: ResultSet, rowNum: Int): GetAssignedCouriersResult =
        GetAssignedCouriersResult(
            courierId = UUID.fromString(rs.getString("id")),
            type = CourierTypeResult.valueOf(rs.getString("type")),
            location = Location.restore(
                rs.getInt("location_x"),
                rs.getInt("location_y"),
            ).let { location ->
                LocationResult(
                    x = location.x,
                    y = location.y,
                )
            },
        )
}

sealed class AssignedCouriersError(override val message: String) : BusinessError {
    data object NoAssignedCouriers : AssignedCouriersError("No assigned couriers found")
}
