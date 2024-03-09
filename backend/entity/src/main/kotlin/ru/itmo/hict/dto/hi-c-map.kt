package ru.itmo.hict.dto

import ru.itmo.hict.dto.UserInfoDto.Companion.toInfoDto
import ru.itmo.hict.entity.HiCMap
import java.util.Date

data class HiCMapInfoDto(
    val id: Long,
    val author: UserInfoDto,
    val meta: HiCMetaDto,
    val views: Long,
) {
    companion object {
        fun HiCMap.toInfoDto(): HiCMapInfoDto =
            HiCMapInfoDto(id!!, author.toInfoDto(),
                HiCMetaDto(name, description, creationTime!!), views?.count ?: 0)
    }
}

data class HiCMetaDto(
    val name: String,
    val description: String,
    val creationTime: Date,
)
