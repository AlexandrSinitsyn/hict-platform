package ru.itmo.hict.scheduler.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.common.serialization.Deserializer
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
    class HiCTMessageDeserializer : Deserializer<HiCTMessageType> {
        private val CREATE_TYPE: Byte = 1
        private val PING_TYPE: Byte = 2
        override fun deserialize(topic: String, data: ByteArray): HiCTMessageType = when (data[0]) {
            CREATE_TYPE -> Create(data.drop(1).toString())
            PING_TYPE -> Ping(data.drop(1).toString())
            else -> throw IllegalArgumentException("Unknown message type (1 - Create, 2 - Ping), but was ${data[0]}")
        }
    }

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
