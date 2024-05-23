package ru.itmo.hict.server.service

import org.springframework.stereotype.Service
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.entity.ContactMap
import ru.itmo.hict.server.exception.InvalidFileTypeException
import ru.itmo.hict.server.exception.NoContactMapFoundException
import ru.itmo.hict.server.exception.SameFieldException
import ru.itmo.hict.server.repository.ContactMapRepository
import ru.itmo.hict.server.repository.ViewsRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class ContactMapService(
    private val contactMapRepository: ContactMapRepository,
    private val experimentService: ExperimentService,
    private val viewsRepository: ViewsRepository,
    private val fileService: FileService,
) {
    fun getByName(name: String): ContactMap? = contactMapRepository.findByName(name).getOrNull()

    fun create(experimentName: String): ContactMap? =
        experimentService.getByName(experimentName)?.run {
            contactMapRepository.save(ContactMap(UUID.randomUUID().toString(), this))
        }

    fun view(contactMap: ContactMap) = viewsRepository.viewById(contactMap.id!!)

    fun updateName(id: Long, newName: String) {
        val selected = contactMapRepository.findById(id).orElse(null)
            ?: throw NoContactMapFoundException(id)

        if (selected.name == newName) {
            throw SameFieldException("name", newName)
        }

        contactMapRepository.updateName(selected, newName)
    }

    fun updateInfo(id: Long, description: String?, link: String?) {
        val selected = contactMapRepository.findById(id).orElse(null)
            ?: throw NoContactMapFoundException(id)

        contactMapRepository.updateInfo(selected, description, link)
    }

    fun attachToContactMap(contactMapId: Long, fileId: UUID, fileType: FileType) {
        val contactMap = contactMapRepository.findById(contactMapId).orElse(null)
            ?: throw NoContactMapFoundException(contactMapId)

        val attach: (map: ContactMap, fileId: UUID) -> Unit = when (fileType) {
            FileType.HIC -> fileService::attachHicFileToContactMap
            FileType.MCOOL -> fileService::attachMcoolFileToContactMap
            FileType.AGP -> fileService::attachAgpFileToContactMap
            FileType.TRACKS -> fileService::attachTracksFileToContactMap
            FileType.FASTA -> throw InvalidFileTypeException(FileType.FASTA)
        }

        attach(contactMap, fileId)
    }
}
