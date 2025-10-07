package delivery.api.input.adapters.http

import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        log.warn("Validation error: {}", ex.message)

        return ResponseEntity
            .badRequest()
            .body(ErrorResponse(ex.message ?: "Invalid input"))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unhandled error: {}", ex.message, ex)

        return ResponseEntity
            .internalServerError()
            .body(ErrorResponse("Unexpected server error"))
    }
}

data class ErrorResponse(
    val message: String
)
