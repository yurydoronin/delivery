package delivery.api.input.adapters.job

import delivery.core.application.ports.input.commands.AssignOrderUseCase
import delivery.core.application.ports.input.commands.CouriersMovementUseCase
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
@DisallowConcurrentExecution // Гарантирует строго последовательный запуск тиков, если транзакции будут выполняться более 2 секунд.
class DeliveryTickJob(
    private val assignOrderUseCase: AssignOrderUseCase,
    private val moveCourierUseCase: CouriersMovementUseCase,
) : Job {
    override fun execute(context: JobExecutionContext) {
        // Сначала строго распределяем новые заказы
        assignOrderUseCase.execute()

        // Только после завершения назначения двигаем курьеров
        moveCourierUseCase.execute()
    }
}
