package ru.itmo.hict.server.service

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import ru.itmo.hict.entity.User
import ru.itmo.hict.messaging.Create
import ru.itmo.hict.messaging.HiCTMessageType
import ru.itmo.hict.messaging.Ping

@Service
class KafkaPublisher(
    private val kafkaTemplate: KafkaTemplate<String, HiCTMessageType>,
) {
    fun publish(user: User) = publish(Create("u${user.id}"))

    fun ping(user: User) = kafkaTemplate.send("hict-docker", Ping("u${user.id}"))

    private fun publish(message: HiCTMessageType) {
        kafkaTemplate.send("hict-docker", message)
    }
}
