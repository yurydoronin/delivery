package delivery.bootstrap

import delivery.domain.services.OrderDispatcherImpl
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(
    scanBasePackages = [
        "delivery.api",
        "delivery.domain",
        "delivery.application",
        "delivery.infrastructure",
    ]
)
@ConfigurationPropertiesScan
@EnableScheduling
class DeliveryApplication

fun main(args: Array<String>) {
    runApplication<DeliveryApplication>(*args)
}

@Configuration
class DomainConfig {

    @Bean
    fun orderDispatcher() = OrderDispatcherImpl()
}