package ru.itmo.hict.authorization.service

import org.springframework.stereotype.Service
import ru.itmo.hict.authorization.repositories.UserRepository
import ru.itmo.hict.entity.Role
import ru.itmo.hict.entity.User
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun checkUnique(login: String, email: String): Boolean =
        !userRepository.findByLoginOrEmail(login, email).isPresent

    fun findByCredentials(login: String?, email: String?, passwordSha: String): Optional<User> =
        userRepository.findByLoginOrEmailAndPasswordSha(login, email, passwordSha)

    fun register(
        username: String,
        login: String,
        email: String,
        passwordSha: String,
    ): User? = userRepository.save(User(username, login, email, passwordSha, Role.USER)).getOrNull()
}
