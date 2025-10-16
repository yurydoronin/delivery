package delivery.infrastructure.output.adapters.grpc

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.grpc.geo-service")
data class GeoServiceProperties(
    val host: String,
    val port: Int
)
