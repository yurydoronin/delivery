package delivery.infrastructure.output.adapters.grpc

import arrow.core.Either
import arrow.core.raise.either
import clients.geo.GeoGrpc
import clients.geo.GetGeolocationRequest
import clients.geo.locationOrNull
import delivery.application.ports.output.GeoServiceClientError
import delivery.application.ports.output.GeoServiceClientPort
import delivery.domain.kernel.Location
import delivery.domain.kernel.LocationError
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

        val grpcLocation = response.locationOrNull
            ?: raise(GeoServiceClientError.LocationNotFound)

        grpcLocation.toDomain()
            .mapLeft(GeoServiceClientError::InvalidLocation)
            .bind()
    }
}

fun clients.geo.Location.toDomain(): Either<LocationError, Location> =
    Location.of(x, y)