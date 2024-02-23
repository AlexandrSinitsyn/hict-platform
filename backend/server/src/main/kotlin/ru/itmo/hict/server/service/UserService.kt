package ru.itmo.hict.server.service

import org.springframework.stereotype.Service
import ru.itmo.hict.entity.Role
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.repository.UserRepository
import kotlin.jvm.optionals.getOrNull

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun count() = userRepository.count()

    fun getById(id: Long): User? = userRepository.findById(id).getOrNull()

    fun checkCredentials(user: User, password: String) =
        userRepository.findByLoginAndPassword(user.login, password).isPresent

    fun updateUsername(user: User, username: String): Boolean {
        userRepository.updateUsername(user.id!!, username)

        return getById(user.id!!)!!.username == username
    }

    fun updateLogin(user: User, login: String): Boolean {
        userRepository.updateLogin(user.id!!, login)

        return getById(user.id!!)!!.login == login
    }

    fun updateEmail(user: User, email: String): Boolean {
        userRepository.updateEmail(user.id!!, email)

        return getById(user.id!!)!!.email == email
    }

    fun updateRole(user: User, role: Role): Boolean {
        userRepository.updateRole(user.id!!, role)

        return getById(user.id!!)!!.role == role
    }

    fun updatePassword(user: User, oldPassword: String, newPassword: String): Boolean {
        userRepository.updatePassword(user.id!!, oldPassword, newPassword)

        return userRepository.findByLoginAndPassword(user.login, newPassword).isPresent
    }
}
