package ru.itmo.hict.server.service

import org.springframework.stereotype.Service
import ru.itmo.hict.entity.Experiment
import ru.itmo.hict.entity.Group
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.exception.NoExperimentFoundException
import ru.itmo.hict.server.exception.NoGroupFoundException
import ru.itmo.hict.server.exception.NotGroupMemberException
import ru.itmo.hict.server.exception.SameFieldException
import ru.itmo.hict.server.logging.Logger
import ru.itmo.hict.server.repository.ExperimentRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class ExperimentService(
    private val logger: Logger,
    private val experimentRepository: ExperimentRepository,
    private val groupService: GroupService,
    private val fileService: FileService,
) {
    fun getAll(groups: List<Group>?): List<Experiment> = experimentRepository.findAll().run {
        val public = this.filter { it.visibilityGroup.name == "public" }

        if (groups.isNullOrEmpty()) {
                return@run public
        }

        public + this.filter { it.visibilityGroup in groups }
    }

    fun getByName(name: String): Experiment? = experimentRepository.findByName(name).getOrNull()

    fun getById(id: UUID): Experiment? = experimentRepository.findById(id).getOrNull()

    fun create(author: User, groupName: String): Experiment {
        val group = groupService.getByName(groupName) ?: throw NoGroupFoundException(groupName)

        if (groupName != "public" && !author.groups.contains(group)) {
            throw NotGroupMemberException(author.username, groupName)
        }

        return experimentRepository.save(Experiment(UUID.randomUUID().toString(), author, group))
    }

    fun updateName(id: UUID, newName: String) {
        val selected = experimentRepository.findById(id).getOrNull()
            ?: throw NoExperimentFoundException(id)

        if (selected.name == newName) {
            throw SameFieldException("experiment", "name", newName)
        }

        experimentRepository.updateName(selected, newName)
    }

    fun updateInfo(id: UUID, description: String?, paper: String?, acknowledgement: String?) {
        val selected = experimentRepository.findById(id).getOrNull()
            ?: throw NoExperimentFoundException(id)

        experimentRepository.updateInfo(selected, description, paper, acknowledgement)
    }

    fun attachToExperiment(experimentId: UUID, fileId: UUID) {
        val experiment = experimentRepository.findById(experimentId).getOrNull()
            ?: throw NoExperimentFoundException(experimentId)

        logger.info("attach", "file", "file[$fileId] of [FASTA] to experiment[$experimentId]")

        fileService.attachFastaFileToExperiment(experiment, fileId)
    }
}
