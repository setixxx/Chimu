package software.setixx.chimu.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@Configuration
class SchedulerConfig {

    @Bean
    fun taskScheduler(): TaskScheduler {
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.poolSize = 4
        scheduler.setThreadNamePrefix("jam-scheduler-")
        scheduler.setWaitForTasksToCompleteOnShutdown(false)
        scheduler.initialize()
        return scheduler
    }
}