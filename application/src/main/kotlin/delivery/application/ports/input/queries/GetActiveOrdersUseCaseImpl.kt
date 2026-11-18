package delivery.application.ports.input.queries

import arrow.core.Either
import arrow.core.raise.either
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
class GetActiveOrdersUseCaseImpl(
    private val jdbcClient: JdbcClient,
) : GetActiveOrdersUseCase {

    @Transactional(readOnly = true)
    override fun execute(): Either<BusinessError, List<GetActiveOrdersResult>> = either {
        val results = jdbcClient.sql(
            """
            SELECT id, location_x, location_y
            FROM orders
            WHERE status <> :status
            """.trimIndent()
        )
            .param("status", OrderStatus.COMPLETED.name)
            .query(::toGetActiveOrdersResult)
            .list()

        if (results.isEmpty()) raise(ActiveOrdersError.NoActiveOrders)

        results
    }

    private fun toGetActiveOrdersResult(rs: ResultSet, rowNum: Int): GetActiveOrdersResult =
        GetActiveOrdersResult(
            orderId = UUID.fromString(rs.getString("id")),
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

sealed class ActiveOrdersError(override val message: String) : BusinessError {
    data object NoActiveOrders : ActiveOrdersError("No active orders found")
}