package ru.itmo.hict.dto

import ru.itmo.hict.entity.User

data class UserInfoDto(
    val id: Long,
    val username: String,
    val login: String,
    val email: String,
) {
    companion object {
        fun User.toInfoDto(): UserInfoDto = UserInfoDto(id!!, username, login, email)
    }
}
