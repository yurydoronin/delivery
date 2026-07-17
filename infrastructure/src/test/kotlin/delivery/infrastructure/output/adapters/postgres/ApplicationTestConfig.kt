package delivery.infrastructure.output.adapters.postgres

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ComponentScan(
    basePackages = [
        "delivery.infrastructure.output.adapters.postgres",
    ]
)
@EnableAutoConfiguration
@Import(FlywayTestConfig::class)
class ApplicationTestConfig
