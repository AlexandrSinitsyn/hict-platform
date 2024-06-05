package ru.itmo.hict.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import ru.itmo.hict.entity.*
import java.util.UUID

@Repository
interface FileRepository : JpaRepository<File, UUID> {
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Query(
        value = """
            select * from save_file(:#{#file.filename}, :#{#file.sequenceLevel.name}, :#{#file.fileSize})
        """,
        nativeQuery = true,
    )
    fun save(@Param("file") file: File): File
}

@Repository
interface HictFileRepository : JpaRepository<HictFile, UUID> {
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Modifying
    @Query(
        value = """
            update contact_maps
            set hict_id = :fileId
            where contact_maps.contact_map_id = :contactMapId
        """,
        nativeQuery = true,
    )
    fun attach(@Param("contactMapId") contactMapId: UUID, @Param("fileId") fileId: UUID)
}

@Repository
interface McoolFileRepository : JpaRepository<McoolFile, UUID> {
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Modifying
    @Query(
        value = """
            update contact_maps
            set mcool_id = :fileId
            where contact_maps.contact_map_id = :contactMapId
        """,
        nativeQuery = true,
    )
    fun attach(@Param("contactMapId") contactMapId: UUID, @Param("fileId") fileId: UUID)
}

@Repository
interface AgpFileRepository : JpaRepository<AgpFile, UUID> {
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Modifying
    @Query(
        value = """
            insert into contact_map_agp (contact_map_id, file_id)
            values (:contactMapId, :fileId)
        """,
        nativeQuery = true,
    )
    fun attach(@Param("contactMapId") contactMapId: UUID, @Param("fileId") fileId: UUID)
}

@Repository
interface TracksFileRepository : JpaRepository<TracksFile, UUID> {
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Modifying
    @Query(
        value = """
            insert into contact_map_tracks (contact_map_id, file_id)
            values (:contactMapId, :fileId)
        """,
        nativeQuery = true,
    )
    fun attach(@Param("contactMapId") contactMapId: UUID, @Param("fileId") fileId: UUID)
}

@Repository
interface FastaFileRepository : JpaRepository<FastaFile, UUID> {
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Modifying
    @Query(
        value = """
            insert into experiment_fasta (experiment_id, file_id)
            values (:experimentId, :fileId)
        """,
        nativeQuery = true,
    )
    fun attach(@Param("experimentId") experimentId: UUID, @Param("fileId") fileId: UUID)
}
