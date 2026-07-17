package delivery.application.ports.input

import javax.sql.DataSource
import org.flywaydb.core.Flyway
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class FlywayTestConfig {

    @Bean(initMethod = "migrate")
    fun flyway(dataSource: DataSource): Flyway =
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
}