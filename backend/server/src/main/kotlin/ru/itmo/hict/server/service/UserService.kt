package ru.itmo.hict.server.service

import org.springframework.stereotype.Service
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.repository.UserRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun count() = userRepository.count()

    fun getById(id: UUID): User? = userRepository.findById(id).getOrNull()

    fun getAll(): List<User> = userRepository.findAll()

    fun checkCredentials(user: User, password: String) =
        userRepository.findByLoginAndPassword(user.login, password).isPresent

    fun isUniqueLoginAndEmail(login: String?, email: String?) =
        userRepository.findByLoginOrEmail(login, email).isEmpty

    fun updateUsername(user: User, username: String) = userRepository.updateUsername(user.id!!, username)

    fun updateLogin(user: User, login: String) = userRepository.updateLogin(user.id!!, login)

    fun updateEmail(user: User, email: String) = userRepository.updateEmail(user.id!!, email)

    fun updatePassword(user: User, oldPassword: String, newPassword: String): Boolean {
        userRepository.updatePassword(user.id!!, oldPassword, newPassword)

        return userRepository.findByLoginAndPassword(user.login, newPassword).isPresent
    }
}
