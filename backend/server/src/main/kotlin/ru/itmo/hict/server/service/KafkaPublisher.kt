package ru.itmo.hict.server.service

import org.apache.kafka.common.serialization.Serializer
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
    class HiCTMessageSerializer : Serializer<HiCTMessageType> {
        override fun serialize(topic: String, data: HiCTMessageType?): ByteArray? = when (data) {
            null -> null
            is Create -> byteArrayOf(1) + data.uid.toByteArray()
            is Ping -> byteArrayOf(2) + data.uid.toByteArray()
        }
    }

    fun publish(user: User) = publish(Create("u${user.id}"))

    fun ping(user: User) = publish(Ping("u${user.id}"))

    private fun publish(message: HiCTMessageType) {
        kafkaTemplate.send("hict-docker", message)
    }
}
