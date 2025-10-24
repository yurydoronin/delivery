package delivery.api.input.adapters.job

import delivery.core.application.ports.input.commands.OrderAssignmentUseCase
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AssignOrdersJob(
    private val useCase: OrderAssignmentUseCase
) : Job {
    @Transactional
    override fun execute(context: JobExecutionContext) {
        useCase.execute()
    }
}
