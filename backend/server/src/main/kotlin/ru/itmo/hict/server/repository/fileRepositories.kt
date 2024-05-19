package ru.itmo.hict.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.itmo.hict.entity.*

@Repository
interface FileRepository : JpaRepository<File, Long>

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
