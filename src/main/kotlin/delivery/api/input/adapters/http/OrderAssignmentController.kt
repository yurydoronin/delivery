package delivery.api.input.adapters.http

import delivery.core.application.ports.input.commands.OrderAssignmentUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/orders/assign")
class OrderAssignmentController(
    private val useCase: OrderAssignmentUseCase
) {
    @PostMapping
    fun assign(): ResponseEntity<String> =
        useCase.assignTo()
            .fold(
                ifLeft = { error -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.message) },
                ifRight = { ResponseEntity.ok().build() }
            )
}
