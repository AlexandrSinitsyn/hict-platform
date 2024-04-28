package ru.itmo.hict.scheduler.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericToStringSerializer
import ru.itmo.hict.scheduler.dto.UserContainerTimeToLive
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}") private val redisHost: String,
    @Value("\${spring.data.redis.port}") private val redisPort: Int,
    @Value("\${spring.data.redis.database}") private val redisDatabase: Int,
    @Value("\${spring.data.redis.password}") private val redisPassword: String,
    @Value("\${spring.data.redis.timeout}") private val redisTimeout: Long,
) {
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory =
        JedisConnectionFactory(
            RedisStandaloneConfiguration().apply {
                hostName = redisHost
                port = redisPort
                database = redisDatabase
                setPassword(redisPassword)
            },
            JedisClientConfiguration.builder()
                .readTimeout(redisTimeout.toDuration(DurationUnit.MILLISECONDS).toJavaDuration())
                .build()
        )

    @Bean
    fun redisTemplate(): RedisTemplate<String, UserContainerTimeToLive> =
        RedisTemplate<String, UserContainerTimeToLive>().apply {
            connectionFactory = redisConnectionFactory()
            valueSerializer = GenericToStringSerializer(UserContainerTimeToLive::class.java)
        }
}
