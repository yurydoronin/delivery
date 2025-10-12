package delivery.api.input.adapters.job

import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QuartzConfig {

    @Bean
    fun assignOrdersJobDetail(): JobDetail =
        JobBuilder.newJob(AssignOrdersJob::class.java)
            .withIdentity("assignOrdersJob")
            .storeDurably()
            .build()

    @Bean
    fun assignOrdersTrigger(assignOrdersJobDetail: JobDetail): Trigger =
        TriggerBuilder.newTrigger()
            .forJob(assignOrdersJobDetail)
            .withIdentity("assignOrdersTrigger")
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(2)
                    .repeatForever()
            )
            .build()

    @Bean
    fun moveCouriersJobDetail(): JobDetail =
        JobBuilder.newJob(MoveCouriersJob::class.java)
            .withIdentity("moveCouriersJob")
            .storeDurably()
            .build()

    @Bean
    fun moveCouriersTrigger(moveCouriersJobDetail: JobDetail): Trigger =
        TriggerBuilder.newTrigger()
            .forJob(moveCouriersJobDetail)
            .withIdentity("moveCouriersTrigger")
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(2)
                    .repeatForever()
            )
            .build()
}