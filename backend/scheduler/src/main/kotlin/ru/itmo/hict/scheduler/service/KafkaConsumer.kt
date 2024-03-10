package ru.itmo.hict.scheduler.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaConsumer(
    private val dindService: DindService,
) {
    @KafkaListener(topics = ["hict-docker"])
    fun dockerRequest(message: String) = runBlocking {
        println("kafka> $message")

        launch {
            val res = dindService.runDocker(message)
            println(res.getOrNull() ?: res.exceptionOrNull())
        }
    }
}
