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
class GetActiveOrdersUseCaseImpl(
    private val jdbcTemplate: JdbcTemplate
) : GetActiveOrdersUseCase {

    @Transactional(readOnly = true)
    override fun execute(): Either<BusinessError, List<GetActiveOrdersResult>> = either {
        val sql = """
            SELECT o.id, o.location_x, o.location_y
            FROM orders o
            WHERE o.status <> 'COMPLETED'
        """.trimIndent()

        val results = jdbcTemplate.query(sql) { rs, _ ->
            GetActiveOrdersResult(
                orderId = UUID.fromString(rs.getString("id")),
                location = Location.of(
                    rs.getInt("location_x"),
                    rs.getInt("location_y")
                )
            )
        }

        if (results.isEmpty()) raise(ActiveOrdersError.NoActiveOrders)

        results
    }
}

sealed class ActiveOrdersError(override val message: String) : BusinessError {
    data object NoActiveOrders : ActiveOrdersError("No active orders found")
}