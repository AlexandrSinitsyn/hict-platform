package ru.itmo.hict.scheduler.dto

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.time.Instant

@RedisHash(
    timeToLive = 1 * 60 * 60 * 3, // 3 hours
)
data class UserContainerTimeToLive(
    @Id
    val userId: String,
    val deadline: Instant,
)
