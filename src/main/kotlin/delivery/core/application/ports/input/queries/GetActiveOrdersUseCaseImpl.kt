package delivery.core.application.ports.input.queries

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import common.types.error.BusinessError
import delivery.core.domain.kernel.Location
import java.util.UUID
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class GetActiveOrdersUseCaseImpl(
    private val jdbcTemplate: JdbcTemplate
) : GetActiveOrdersUseCase {

    override fun execute(): Either<BusinessError, List<GetActiveOrdersResult>> {
        val sql = """
            SELECT o.id, o.location_x, o.location_y
            FROM orders o
            WHERE o.status <> 'COMPLETED'
        """

        val results = jdbcTemplate.query(sql) { rs, _ ->
            GetActiveOrdersResult(
                orderId = UUID.fromString(rs.getString("id")),
                location = Location.of(
                    rs.getInt("location_x"),
                    rs.getInt("location_y")
                )
            )
        }

        return results.takeIf { it.isNotEmpty() }
            ?.right()
            ?: ActiveOrdersError.NoActiveOrders.left()
    }
}

sealed class ActiveOrdersError(override val message: String) : BusinessError {
    data object NoActiveOrders : ActiveOrdersError("No active orders found")
}