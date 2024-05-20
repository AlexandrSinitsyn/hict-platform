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

@Repository
interface FileRepository : JpaRepository<File, Long> {
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying
    @Query(
        value = """
            insert into files (filename, sequence_level, file_size, visibility_group)
            values (:#{#file.filename},
                    cast(:#{#file.sequenceLevel.name} as sequence_level_type),
                    :#{#file.fileSize},
                    :#{#file.visibilityGroup.id})
        """,
        nativeQuery = true,
    )
    fun save(@Param("file") file: File)

    fun findByFilename(filename: String): Optional<File>
}

@Repository
interface HiCFileRepository : JpaRepository<HiCFile, Long>

@Repository
interface McoolFileRepository : JpaRepository<McoolFile, Long>

@Repository
interface AgpFileRepository : JpaRepository<AgpFile, Long>

@Repository
interface TracksFileRepository : JpaRepository<TracksFile, Long>

@Repository
interface FastaFileRepository : JpaRepository<FastaFile, Long>
