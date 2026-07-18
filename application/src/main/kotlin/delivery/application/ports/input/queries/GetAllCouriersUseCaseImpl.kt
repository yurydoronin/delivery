package delivery.application.ports.input.queries

import arrow.core.Either
import arrow.core.raise.either
import delivery.common.types.dto.LocationResult
import delivery.common.types.error.BusinessError
import delivery.domain.kernel.Location
import java.util.UUID
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetAllCouriersUseCaseImpl(
    private val jdbcTemplate: JdbcTemplate
) : GetAllCouriersUseCase {

    @Transactional(readOnly = true)
    override fun execute(): Either<BusinessError, List<GetAllCouriersResult>> = either {
        val sql = """
            SELECT id, name, location_x, location_y
            FROM couriers
        """.trimIndent()

        val results = jdbcTemplate.query(sql) { rs, _ ->
            val domainLocation = Location.restore(
                rs.getInt("location_x"),
                rs.getInt("location_y")
            )

            GetAllCouriersResult(
                courierId = UUID.fromString(rs.getString("id")),
                name = rs.getString("name"),
                location = LocationResult(
                    x = domainLocation.x,
                    y = domainLocation.y
                )
            )
        }

        if (results.isEmpty()) raise(CouriersError.NoCouriersFound)

        results
    }
}

sealed class CouriersError(override val message: String) : BusinessError {
    data object NoCouriersFound : CouriersError("No couriers found")
}
