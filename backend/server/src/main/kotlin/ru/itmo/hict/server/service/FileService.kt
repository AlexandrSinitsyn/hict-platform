package ru.itmo.hict.server.service

import org.springframework.stereotype.Service
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.entity.*
import ru.itmo.hict.server.repository.*
import java.util.*

@Service
class FileService(
    private val groupService: GroupService,
    private val fileRepository: FileRepository,
    private val hicFileRepository: HiCFileRepository,
    private val mcoolFileRepository: McoolFileRepository,
    private val agpFileRepository: AgpFileRepository,
    private val tracksFileRepository: TracksFileRepository,
    private val fastaFileRepository: FastaFileRepository,
) {
    fun save(fileType: FileType, filename: String, fileSize: Long): AttachedFile {
        val visibilityGroup = groupService.getByName("public")!!

        // fixme
        fileRepository.save(File(filename, SequenceLevelType.SCAFFOLD, fileSize, visibilityGroup))

        val saved = fileRepository.findByFilename(filename).orElseThrow()

        return when (fileType) {
            FileType.HIC -> hicFileRepository.save(HiCFile(saved))
            FileType.MCOOL -> mcoolFileRepository.save(McoolFile(saved))
            FileType.AGP -> agpFileRepository.save(AgpFile(saved))
            FileType.TRACKS -> tracksFileRepository.save(TracksFile(saved))
            FileType.FASTA -> fastaFileRepository.save(FastaFile(saved))
        }
    }

    fun attachFastaFileToExperiment(experiment: Experiment, uuid: UUID) {}
    fun attachHicFileToContactMap(contactMap: ContactMap, uuid: UUID) {}
    fun attachMcoolFileToContactMap(contactMap: ContactMap, uuid: UUID) {}
    fun attachAgpFileToContactMap(contactMap: ContactMap, uuid: UUID) {}
    fun attachTracksFileToContactMap(contactMap: ContactMap, uuid: UUID) {}
}
