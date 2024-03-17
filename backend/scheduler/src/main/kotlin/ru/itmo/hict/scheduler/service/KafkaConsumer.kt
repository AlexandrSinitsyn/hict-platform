package ru.itmo.hict.scheduler.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import ru.itmo.hict.messaging.Create
import ru.itmo.hict.messaging.HiCTMessageType
import ru.itmo.hict.messaging.Ping
import ru.itmo.hict.scheduler.logging.Logger

@Service
class KafkaConsumer(
    private val dindService: DindService,
    private val containerMonitor: ContainerMonitor,
    private val logger: Logger,
) {
    @KafkaListener(topics = ["hict-docker"])
    fun dockerRequest(request: HiCTMessageType) = runBlocking {
        logger.info("kafka", "accepted", "$request")

        when (request) {
            is Create -> {
                containerMonitor.register(request.uid)

                launch {
                    val res = dindService.runDocker(request.uid)
                    logger.info("kafka", "response", when {
                        res.isSuccess -> "success"
                        else -> res.exceptionOrNull()!!.run { this.message ?: toString() }
                    })
                }
            }
            is Ping -> containerMonitor.extend(request.uid)
        }

        Unit
    }
}
