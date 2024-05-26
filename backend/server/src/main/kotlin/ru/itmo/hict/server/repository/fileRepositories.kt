package ru.itmo.hict.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import ru.itmo.hict.entity.*
import java.util.Optional
import java.util.UUID

@Repository
interface FileRepository : JpaRepository<File, UUID> {
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying
    @Query(
        value = """
            insert into files (filename, sequence_level, file_size)
            values (:#{#file.filename},
                    cast(:#{#file.sequenceLevel.name} as sequence_level_type),
                    :#{#file.fileSize})
        """,
        nativeQuery = true,
    )
    fun save(@Param("file") file: File)

    fun findByFilename(filename: String): Optional<File>
}

@Repository
interface HictFileRepository : JpaRepository<HictFile, UUID>

@Repository
interface McoolFileRepository : JpaRepository<McoolFile, UUID>

@Repository
interface AgpFileRepository : JpaRepository<AgpFile, UUID>

@Repository
interface TracksFileRepository : JpaRepository<TracksFile, UUID>

@Repository
interface FastaFileRepository : JpaRepository<FastaFile, UUID>
