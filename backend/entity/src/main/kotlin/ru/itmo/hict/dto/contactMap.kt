package ru.itmo.hict.dto

import ru.itmo.hict.dto.FileInfoDto.Companion.toInfoDto
import ru.itmo.hict.entity.*
import java.util.Date
import java.util.UUID

data class ContactMapInfoDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val link: String?,
    // fixme
    val hict: FileInfoDto?,
    val agp: List<FileInfoDto>,
    val mcool: FileInfoDto?,
    val tracks: List<FileInfoDto>,
    val creationTime: Date
) {
    companion object {
        fun ContactMap.toInfoDto(): ContactMapInfoDto =
            ContactMapInfoDto(id!!, name, description, hicDataLink, hict?.toInfoDto(),
                agp.map { it.toInfoDto() }, mcool?.toInfoDto(), tracks.map { it.toInfoDto() }, creationTime!!)
    }
}
