package ru.itmo.hict.dto

import ru.itmo.hict.dto.ContactMapInfoDto.Companion.toInfoDto
import ru.itmo.hict.dto.FileInfoDto.Companion.toInfoDto
import ru.itmo.hict.dto.UserInfoDto.Companion.toInfoDto
import ru.itmo.hict.entity.Experiment
import java.util.Date
import java.util.UUID

data class ExperimentInfoDto(
    val id: UUID,
    val name: String,
    val author: UserInfoDto,
    val description: String?,
    val link: String?,
    val acknowledgement: String?,
    val contactMaps: List<ContactMapInfoDto>,
    val fasta: List<FileInfoDto>,
    val creationTime: Date
) {
    companion object {
        fun Experiment.toInfoDto(): ExperimentInfoDto =
            ExperimentInfoDto(id!!, name, author.toInfoDto(), description, paper, acknowledgement,
                contactMaps.map { it.toInfoDto() }, fasta.map { it.toInfoDto() }, creationTime!!)
    }
}
