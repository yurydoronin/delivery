package delivery.api.input.adapters.job

import delivery.core.application.ports.input.commands.OrderAssignmentUseCase
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class AssignOrdersJob(
    private val useCase: OrderAssignmentUseCase
) : Job {
    override fun execute(context: JobExecutionContext) {
        useCase.assignTo()
    }
}
