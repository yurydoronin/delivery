package delivery.application.ports.input

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ComponentScan(
    basePackages = [
        "delivery.application.ports.input.queries",
    ]
)
@EnableAutoConfiguration
@Import(FlywayTestConfig::class)
class ApplicationTestConfig
