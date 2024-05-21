package ru.itmo.hict.dto

import ru.itmo.hict.dto.UserInfoDto.Companion.toInfoDto
import ru.itmo.hict.entity.Group
import ru.itmo.hict.entity.User
import java.util.*

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

data class GroupInfoDto(
    val id: Long,
    val name: String,
    val users: List<UserInfoDto>,
    val creationTime: Date,
) {
    companion object {
        fun Group.toInfoDto(): GroupInfoDto = GroupInfoDto(id!!, name, users.map { it.toInfoDto() }, creationTime!!)
    }
}
