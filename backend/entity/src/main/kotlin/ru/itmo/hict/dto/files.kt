package ru.itmo.hict.dto

import ru.itmo.hict.entity.AttachedFile
import ru.itmo.hict.entity.File
import ru.itmo.hict.entity.SequenceLevelType
import java.util.*

enum class FileType(val bucket: String, val extension: String = bucket) {
    HICT("hict", "hict.hdf5"),
    MCOOL("mcool"),
    AGP("agp"),
    TRACKS("tracks", "bed"),
    FASTA("fasta")
}

data class FileInfoDto(
    val id: UUID,
    val filename: String,
    val sequenceLevel: SequenceLevelType,
    val filesize: Long,
    val creationTime: Date
) {
    companion object {
        fun File.toInfoDto(): FileInfoDto =
            FileInfoDto(id!!, filename, sequenceLevel, fileSize, creationTime!!)

        fun AttachedFile.toInfoDto(): FileInfoDto = this.file.toInfoDto()
    }
}
