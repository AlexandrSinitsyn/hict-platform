package ru.itmo.hict.server.service

import org.springframework.stereotype.Service
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.repository.UserRepository
import kotlin.jvm.optionals.getOrNull

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun getById(id: Long): User? = userRepository.findById(id).getOrNull()
}
