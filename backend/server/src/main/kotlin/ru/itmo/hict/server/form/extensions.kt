package ru.itmo.hict.server.form

import ru.itmo.hict.entity.Role
import ru.itmo.hict.entity.User

data class UserExtendedInfo(
    val id: Long,
    val username: String,
    val login: String,
    val email: String,
    val role: Role,
) {
    companion object {
        fun User.toExtendedInfo() = UserExtendedInfo(id!!, username, login, email, role)
    }
}

