package ru.itmo.hict.scheduler.scheduler

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.itmo.hict.scheduler.logging.Logger
import ru.itmo.hict.scheduler.service.ContainerMonitor
import ru.itmo.hict.scheduler.service.DindService

@Component
class Scheduler(
    private val containerMonitor: ContainerMonitor,
    private val dindService: DindService,
    private val logger: Logger,
) {
    @Scheduled(cron = "*/15 * * * * *")
    fun testDeadline() {
        runBlocking {
            logger.info("scheduler", "deadline", "check")

            containerMonitor.reachedDeadline().forEach {
                logger.info("scheduler", "deadline", "found inactive: $it")

                launch {
                    containerMonitor.deregister(it)
                    dindService.stopDocker(it)
                }
            }
        }
    }
}
