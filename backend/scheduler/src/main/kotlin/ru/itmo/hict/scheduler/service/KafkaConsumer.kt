package ru.itmo.hict.scheduler.service

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaConsumer {
    @KafkaListener(topics = ["hict-docker"])
    fun dockerRequest(message: String) {
        println("kafka> $message")
    }
}
