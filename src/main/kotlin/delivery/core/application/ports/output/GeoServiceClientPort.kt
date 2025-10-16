package delivery.core.application.ports.output

import arrow.core.Either
import delivery.core.domain.kernel.Location
import delivery.infrastructure.output.adapters.grpc.GeoServiceClientError

interface GeoServiceClientPort {
    fun getLocation(street: String): Either<GeoServiceClientError, Location>
}