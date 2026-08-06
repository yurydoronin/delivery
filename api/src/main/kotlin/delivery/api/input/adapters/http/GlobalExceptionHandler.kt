package delivery.api.input.adapters.http

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class GlobalExceptionHandler {

    private val log = KotlinLogging.logger {}

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<String> {
        log.warn { "Validation error: ${ex.message}" }

        return ResponseEntity
            .badRequest()
            .body(ex.message ?: "Invalid input")
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<String> {
        log.error(ex) { "Unhandled error: ${ex.message}" }

        return ResponseEntity
            .internalServerError()
            .body("Unexpected server error: ${ex.cause}")
    }
}
