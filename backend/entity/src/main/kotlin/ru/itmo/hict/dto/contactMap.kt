package ru.itmo.hict.dto

import ru.itmo.hict.entity.*
import java.util.Date
import java.util.UUID

data class ContactMapInfoDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val link: String?,
    // fixme
    val hic: HictFile?,
    val agp: List<AgpFile>,
    val mcool: McoolFile?,
    val tracks: List<TracksFile>,
    val creationTime: Date
) {
    companion object {
        fun ContactMap.toInfoDto(): ContactMapInfoDto =
            ContactMapInfoDto(id!!, name, description, hicDataLink, hic, agp, mcool, tracks, creationTime!!)
    }
}
