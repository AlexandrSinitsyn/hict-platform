package ru.itmo.hict.scheduler.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import ru.itmo.hict.scheduler.logging.Logger

@Service
class KafkaConsumer(
    private val dindService: DindService,
    private val logger: Logger,
) {
    @KafkaListener(topics = ["hict-docker"])
    fun dockerRequest(message: String) = runBlocking {
        logger.info("kafka", "accepted", message)

        launch {
            val res = dindService.runDocker(message)
            logger.info("kafka", "response", when {
                res.isSuccess -> "success"
                else -> res.exceptionOrNull()!!.run { this.message ?: toString() }
            })
        }
    }
}
