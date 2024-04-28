package ru.itmo.hict.scheduler.service

import org.springframework.stereotype.Component
import ru.itmo.hict.scheduler.dto.UserContainerTimeToLive
import ru.itmo.hict.scheduler.repository.UcttlRepository
import java.time.Instant
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

@Component
class ContainerMonitor(
    private val ucttlRepository: UcttlRepository,
) {
    private val timeToLive = 10.toDuration(DurationUnit.MINUTES).toJavaDuration()

    private val now: Instant
        get() = Instant.now()

    fun reachedDeadline() = ucttlRepository.findAll().filter { (_, t) -> t.isBefore(now) }.map { it.userId }

    fun register(id: String) = ucttlRepository.save(UserContainerTimeToLive(id, now + timeToLive))

    fun extend(id: String) = ucttlRepository.save(UserContainerTimeToLive(id, now + timeToLive))

    fun deregister(id: String) = ucttlRepository.deleteById(id)
}
