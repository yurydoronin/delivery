package delivery.application.ports.output

import arrow.core.Either
import delivery.common.types.error.BusinessError
import delivery.domain.kernel.Location

interface GeoServiceClientPort {
    fun getLocation(street: String): Either<GeoServiceClientError, Location>
}

sealed class GeoServiceClientError(override val message: String) : BusinessError {
    data object LocationNotFound : GeoServiceClientError("Location not found")
}
