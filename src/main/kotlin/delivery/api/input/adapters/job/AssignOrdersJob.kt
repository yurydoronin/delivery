package delivery.api.input.adapters.job

import delivery.core.application.ports.input.commands.AssignOrderUseCase
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AssignOrdersJob(
    private val useCase: AssignOrderUseCase
) : Job {
    @Transactional
    override fun execute(context: JobExecutionContext) {
        useCase.execute()
    }
}
