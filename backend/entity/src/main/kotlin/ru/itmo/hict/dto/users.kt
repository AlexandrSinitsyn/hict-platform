package ru.itmo.hict.dto

import ru.itmo.hict.dto.GroupInfoDto.Companion.toInfoDto
import ru.itmo.hict.entity.Group
import ru.itmo.hict.entity.User
import java.util.*

data class UserInfoDto(
    val id: UUID,
    val username: String,
    val login: String,
    val email: String,
    val groups: List<GroupInfoDto>
) {
    companion object {
        fun User.toInfoDto(): UserInfoDto = UserInfoDto(id!!, username, login, email, groups.map { it.toInfoDto() })
    }
}

data class GroupInfoDto(
    val id: UUID,
    val name: String,
    val affiliation: String?,
    val creationTime: Date,
) {
    companion object {
        fun Group.toInfoDto(): GroupInfoDto = GroupInfoDto(id!!, name, affiliation, creationTime!!)
    }
}
