package delivery.api.input.adapters.job

import org.quartz.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QuartzConfig {

    @Bean
    fun deliveryTickJobDetail(): JobDetail =
        JobBuilder.newJob(DeliveryTickJob::class.java)
            .withIdentity("deliveryTickJob")
            .storeDurably()
            .build()

    @Bean
    fun deliveryTickTrigger(deliveryTickJobDetail: JobDetail): Trigger =
        TriggerBuilder.newTrigger()
            .forJob(deliveryTickJobDetail)
            .withIdentity("deliveryTickTrigger")
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(2)
                    .repeatForever()
            )
            .build()

//    @Bean
//    fun assignOrdersJobDetail(): JobDetail =
//        JobBuilder.newJob(AssignOrdersJob::class.java)
//            .withIdentity("assignOrdersJob")
//            .storeDurably()
//            .build()
//
//    @Bean
//    fun assignOrdersTrigger(assignOrdersJobDetail: JobDetail): Trigger =
//        TriggerBuilder.newTrigger()
//            .forJob(assignOrdersJobDetail)
//            .withIdentity("assignOrdersTrigger")
//            .withSchedule(
//                SimpleScheduleBuilder.simpleSchedule()
//                    .withIntervalInSeconds(2)
//                    .repeatForever()
//            )
//            .build()
//
//    @Bean
//    fun moveCouriersJobDetail(): JobDetail =
//        JobBuilder.newJob(MoveCouriersJob::class.java)
//            .withIdentity("moveCouriersJob")
//            .storeDurably()
//            .build()
//
//    @Bean
//    fun moveCouriersTrigger(moveCouriersJobDetail: JobDetail): Trigger =
//        TriggerBuilder.newTrigger()
//            .forJob(moveCouriersJobDetail)
//            .withIdentity("moveCouriersTrigger")
//            .withSchedule(
//                SimpleScheduleBuilder.simpleSchedule()
//                    .withIntervalInSeconds(2)
//                    .repeatForever()
//            )
//            .build()
}