package ru.itmo.hict.authorization.service

import org.springframework.stereotype.Service
import ru.itmo.hict.authorization.repository.UserRepository
import ru.itmo.hict.entity.User
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun checkUnique(login: String, email: String): Boolean =
        !userRepository.findByLoginOrEmail(login, email).isPresent

    fun findByCredentials(login: String?, email: String?, password: String): Optional<User> =
        userRepository.findByLoginOrEmailAndPassword(login, email, password)

    fun register(
        username: String,
        login: String,
        email: String,
        password: String,
    ): User? = userRepository.save(User(username, login, email, password)).getOrNull()
}
