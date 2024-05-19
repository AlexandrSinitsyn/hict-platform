package ru.itmo.hict.dto

import ru.itmo.hict.entity.*
import java.util.Date

data class ContactMapInfoDto(
    val id: Long,
    val name: String,
    val description: String?,
    val link: String?,
    // fixme
    val hic: HiCFile?,
    val agp: List<AgpFile>,
    val mcool: McoolFile?,
    val tracks: List<TracksFile>,
    val creationTime: Date
) {
    companion object {
        fun ContactMap.toInfoDto(): ContactMapInfoDto =
            ContactMapInfoDto(id!!, name, description, hicDataLink, hict, agp, mcool, tracks, creationTime!!)
    }
}
