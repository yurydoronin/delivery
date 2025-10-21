package delivery.api.input.adapters.http.cors

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {

    @Bean
    fun corsFilter() = CorsFilter(
        UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", CorsConfiguration().apply {
                allowCredentials = true
                addAllowedOriginPattern("http://localhost:8086/")
                addAllowedHeader("*")
                addAllowedMethod("*")
            })
        }
    )
}
