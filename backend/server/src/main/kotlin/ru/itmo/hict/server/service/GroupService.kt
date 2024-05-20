package ru.itmo.hict.server.service

import org.springframework.stereotype.Service
import ru.itmo.hict.entity.Group
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.repository.GroupRepository
import ru.itmo.hict.server.repository.UserRepository
import kotlin.jvm.optionals.getOrNull

@Service
class GroupService(
    private val groupRepository: GroupRepository,
) {
    fun getByName(name: String): Group? = groupRepository.getByName(name).getOrNull()
}
