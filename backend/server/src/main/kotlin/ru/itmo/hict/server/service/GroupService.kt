package ru.itmo.hict.server.service

import org.springframework.stereotype.Service
import ru.itmo.hict.entity.Group
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.exception.NoSuchEntityException
import ru.itmo.hict.server.repository.GroupRepository
import kotlin.jvm.optionals.getOrNull

@Service
class GroupService(
    private val groupRepository: GroupRepository,
) {
    fun getByName(name: String): Group? = groupRepository.getByName(name).getOrNull()

    fun getAll(): List<Group> = groupRepository.findAll()

    fun create(user: User, name: String): Group = groupRepository.save(Group(name, listOf(user)))

    fun updateName(name: String, newName: String) {
        if (!groupRepository.existsByName(name)) {
            throw NoSuchEntityException("name", name)
        }

        groupRepository.updateName(name, newName)
    }

    fun join(user: User, name: String) = groupRepository.join(name, user)
}
