package delivery.api.input.adapters.job

import delivery.core.application.ports.input.commands.CouriersMovementUseCase
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class MoveCouriersJob(
    private val useCase: CouriersMovementUseCase
) : Job {
    override fun execute(context: JobExecutionContext) {
        useCase.execute()
    }
}
