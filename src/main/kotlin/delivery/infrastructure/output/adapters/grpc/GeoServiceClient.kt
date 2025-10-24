package delivery.infrastructure.output.adapters.grpc

import arrow.core.Either
import arrow.core.raise.either
import clients.geo.GeoGrpc
import clients.geo.GetGeolocationRequest
import clients.geo.locationOrNull
import common.types.error.BusinessError
import delivery.core.application.ports.output.GeoServiceClientPort
import delivery.core.domain.kernel.Location
import io.grpc.ManagedChannelBuilder
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service

@Service
class GeoServiceClient(
    props: GeoServiceProperties
) : GeoServiceClientPort {

    private val channel = ManagedChannelBuilder
        .forAddress(props.host, props.port)
        .usePlaintext()
        .build()

    private val stub = GeoGrpc.newBlockingV2Stub(channel)

    @PreDestroy
    fun shutdown() {
        if (!channel.isShutdown) channel.shutdown()
    }

    override fun getLocation(street: String): Either<GeoServiceClientError, Location> = either {
        val response = stub.getGeolocation(
            GetGeolocationRequest.newBuilder()
                .setStreet(street)
                .build()
        )

        val location = response.locationOrNull
            ?: raise(GeoServiceClientError.LocationNotFound)

        location.toDomain()
    }
}

fun clients.geo.Location.toDomain() = Location.of(x, y)

sealed class GeoServiceClientError(override val message: String) : BusinessError {
    data object LocationNotFound : GeoServiceClientError("Location not found")
}