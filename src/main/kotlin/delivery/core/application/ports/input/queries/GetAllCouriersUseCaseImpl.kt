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
            GetAllCouriersResult(
                courierId = UUID.fromString(rs.getString("id")),
                name = rs.getString("name"),
                location = Location.of(
                    rs.getInt("location_x"),
                    rs.getInt("location_y")
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
