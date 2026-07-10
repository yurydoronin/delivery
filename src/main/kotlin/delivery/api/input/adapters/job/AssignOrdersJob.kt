package delivery.api.input.adapters.job

import delivery.core.application.ports.input.commands.AssignOrderUseCase
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class AssignOrdersJob(
    private val useCase: AssignOrderUseCase
) : Job {
    override fun execute(context: JobExecutionContext) {
        useCase.execute()
    }
}
