package ru.itmo.hict.scheduler.service

import org.springframework.stereotype.Component
import java.time.Instant
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

@Component
class ContainerMonitor {
    private data class UserContainerTimeToLive(
        val userId: String,
        val deadline: Instant,
    )

    private val ucttlRepository: MutableList<UserContainerTimeToLive> = ArrayList()
    private val timeToLive = 10.toDuration(DurationUnit.MINUTES).toJavaDuration()

    private val now: Instant
        get() = Instant.now()

    fun reachedDeadline(): List<String> =
        ucttlRepository.stream().filter { (_, t) -> t.isBefore(now) }.map { it.userId }.toList()

    fun register(id: String) {
        ucttlRepository += UserContainerTimeToLive(id, now + timeToLive)
    }

    fun extend(id: String) {
        ucttlRepository += UserContainerTimeToLive(id, now + timeToLive)
    }

    fun deregister(id: String) = ucttlRepository.removeIf { it.userId == id }
}
