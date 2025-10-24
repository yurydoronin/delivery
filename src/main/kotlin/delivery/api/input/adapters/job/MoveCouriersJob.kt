package delivery.api.input.adapters.job

import delivery.core.application.ports.input.commands.CouriersMovementUseCase
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MoveCouriersJob(
    private val useCase: CouriersMovementUseCase
) : Job {
    @Transactional
    override fun execute(context: JobExecutionContext) {
        useCase.execute()
    }
}
