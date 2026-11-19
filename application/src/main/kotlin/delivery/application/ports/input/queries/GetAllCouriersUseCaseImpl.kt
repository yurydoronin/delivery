package delivery.application.ports.input.queries

import arrow.core.Either
import arrow.core.raise.either
import delivery.application.dto.CourierTypeResult
import delivery.application.dto.LocationResult
import delivery.common.types.error.BusinessError
import delivery.domain.kernel.Location
import java.sql.ResultSet
import java.util.UUID
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetAllCouriersUseCaseImpl(
    private val jdbcClient: JdbcClient,
) : GetAllCouriersUseCase {

    @Transactional(readOnly = true)
    override fun execute(): Either<BusinessError, List<GetAllCouriersResult>> = either {
        val results = jdbcClient.sql(
            """
            SELECT id, type, location_x, location_y
            FROM couriers
            """.trimIndent()
        )
            .query(::toGetAllCouriersResult)
            .list()

        if (results.isEmpty()) raise(CouriersError.NoCouriersFound)

        results
    }

    private fun toGetAllCouriersResult(rs: ResultSet, rowNum: Int): GetAllCouriersResult =
        GetAllCouriersResult(
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

sealed class CouriersError(override val message: String) : BusinessError {
    data object NoCouriersFound : CouriersError("No couriers found")
}
