package ru.itmo.hict.dto

import ru.itmo.hict.entity.Role
import ru.itmo.hict.entity.User

data class UserInfoDto(
    val id: Long,
    val username: String,
    val role: Role,
) {
    companion object {
        fun User.toInfoDto(): UserInfoDto = UserInfoDto(id!!, username, role)
    }
}
