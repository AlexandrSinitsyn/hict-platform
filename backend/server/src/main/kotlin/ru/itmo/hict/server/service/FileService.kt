package ru.itmo.hict.server.service

import org.springframework.stereotype.Service
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.entity.*
import ru.itmo.hict.server.repository.*
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class FileService(
    private val fileRepository: FileRepository,
    private val hictFileRepository: HictFileRepository,
    private val mcoolFileRepository: McoolFileRepository,
    private val agpFileRepository: AgpFileRepository,
    private val tracksFileRepository: TracksFileRepository,
    private val fastaFileRepository: FastaFileRepository,
) {
    fun save(fileType: FileType, filename: String, fileSize: Long): AttachedFile {
        val saved = fileRepository.save(File(filename, SequenceLevelType.SCAFFOLD, fileSize))

        return when (fileType) {
            FileType.HICT -> hictFileRepository.save(HictFile(saved))
            FileType.MCOOL -> mcoolFileRepository.save(McoolFile(saved))
            FileType.AGP -> agpFileRepository.save(AgpFile(saved))
            FileType.TRACKS -> tracksFileRepository.save(TracksFile(saved))
            FileType.FASTA -> fastaFileRepository.save(FastaFile(saved))
        }
    }

    fun attachFastaFileToExperiment(experiment: Experiment, uuid: UUID) =
        fastaFileRepository.attach(experiment.id!!, uuid)
    fun attachHicFileToContactMap(contactMap: ContactMap, uuid: UUID) =
        hictFileRepository.attach(contactMap.id!!, uuid)
    fun attachMcoolFileToContactMap(contactMap: ContactMap, uuid: UUID) =
        mcoolFileRepository.attach(contactMap.id!!, uuid)
    fun attachAgpFileToContactMap(contactMap: ContactMap, uuid: UUID) =
        agpFileRepository.attach(contactMap.id!!, uuid)
    fun attachTracksFileToContactMap(contactMap: ContactMap, uuid: UUID) =
        tracksFileRepository.attach(contactMap.id!!, uuid)
}
