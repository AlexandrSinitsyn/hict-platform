package ru.itmo.hict.server.service

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.entity.Experiment
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.exception.NoExperimentFoundException
import ru.itmo.hict.server.exception.SameFieldException
import ru.itmo.hict.server.repository.ExperimentRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class ExperimentService(
    private val experimentRepository: ExperimentRepository,
    private val minioService: MinioService,
    private val fileService: FileService,
) {
    @PostConstruct
    fun init() {
        FileType.entries.forEach { minioService.newBucketIfAbsent(it.bucket) }
    }

    fun getAll(): List<Experiment> = experimentRepository.findAll()

    fun getByName(name: String): Experiment? = experimentRepository.findByName(name).getOrNull()

    fun create(author: User): Experiment = experimentRepository.save(Experiment(UUID.randomUUID().toString(), author))

    fun updateName(id: Long, newName: String) {
        val selected = experimentRepository.findById(id).orElse(null)
            ?: throw NoExperimentFoundException(id)

        if (selected.name == newName) {
            throw SameFieldException("name", newName)
        }

        experimentRepository.updateName(selected, newName)
    }

    fun updateInfo(id: Long, description: String?, paper: String?, acknowledgement: String?) {
        val selected = experimentRepository.findById(id).orElse(null)
            ?: throw NoExperimentFoundException(id)

        experimentRepository.updateInfo(selected, description, paper, acknowledgement)
    }

    fun attachToExperiment(experimentId: Long, fileId: UUID) {
        val experiment = experimentRepository.findById(experimentId).orElse(null)
            ?: throw NoExperimentFoundException(experimentId)

        fileService.attachFastaFileToExperiment(experiment, fileId)
    }
}
