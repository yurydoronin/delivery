package delivery.core.application

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import common.types.error.BusinessError
import delivery.core.application.ports.input.queries.GetIncompleteOrdersResult
import delivery.core.application.ports.input.queries.GetIncompleteOrdersUseCase
import delivery.core.domain.kernel.Location
import java.util.UUID
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class GetIncompleteOrdersService(
    private val jdbcTemplate: JdbcTemplate
) : GetIncompleteOrdersUseCase {

    override fun getIncompleteOrders(): Either<BusinessError, List<GetIncompleteOrdersResult>> {
        val sql = """
            SELECT o.id, o.location_x, o.location_y
            FROM orders o
            WHERE o.status <> 'COMPLETED'
        """

        val results = jdbcTemplate.query(sql) { rs, _ ->
            GetIncompleteOrdersResult(
                orderId = UUID.fromString(rs.getString("id")),
                location = Location.of(
                    rs.getInt("location_x"),
                    rs.getInt("location_y")
                )
            )
        }

        return results.takeIf { it.isNotEmpty() }
            ?.right()
            ?: IncompleteOrdersError.NoIncompleteOrders.left()

    }
}

sealed class IncompleteOrdersError(override val message: String) : BusinessError {
    data object NoIncompleteOrders : IncompleteOrdersError("No incomplete orders found")
}