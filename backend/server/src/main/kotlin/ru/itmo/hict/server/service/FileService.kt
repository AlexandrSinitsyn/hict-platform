package ru.itmo.hict.server.service

import org.springframework.stereotype.Service
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.entity.*
import ru.itmo.hict.server.repository.*

@Service
class FileService(
    private val fileRepository: FileRepository,
    private val hicFileRepository: HiCFileRepository,
    private val mcoolFileRepository: McoolFileRepository,
    private val agpFileRepository: AgpFileRepository,
    private val tracksFileRepository: TracksFileRepository,
    private val fastaFileRepository: FastaFileRepository,
) {
    fun save(fileType: FileType, file: File): Any {
        val saved = fileRepository.save(file)

        return when (fileType) {
            FileType.HIC -> hicFileRepository.save(HiCFile(saved))
            FileType.MCOOL -> mcoolFileRepository.save(McoolFile(saved))
            FileType.AGP -> agpFileRepository.save(AgpFile(saved))
            FileType.TRACKS -> tracksFileRepository.save(TracksFile(saved))
            FileType.FASTA -> fastaFileRepository.save(FastaFile(saved))
        }
    }
}
