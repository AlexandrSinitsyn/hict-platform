package ru.itmo.hict.server.service

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import ru.itmo.hict.entity.User

@Service
class KafkaPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    fun publish(user: User) = publish("u${user.id}")

    private fun publish(message: String) {
        kafkaTemplate.send("hict-docker", message)
    }
}
