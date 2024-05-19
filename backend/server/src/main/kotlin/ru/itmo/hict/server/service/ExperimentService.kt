package ru.itmo.hict.server.service

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import ru.itmo.hict.entity.Experiment
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.repository.ExperimentRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class ExperimentService(
    private val experimentRepository: ExperimentRepository,
    private val minioService: MinioService,
) {
    @PostConstruct
    fun init() {
        minioService.newBucketIfAbsent("fasta")
    }

    fun getAll(): List<Experiment> = experimentRepository.findAll()

    fun getByName(name: String): Experiment? = experimentRepository.findByName(name).getOrNull()

    fun create(author: User): Experiment = experimentRepository.save(Experiment(UUID.randomUUID().toString(), author))

    // fun updateName(experiment: Experiment, newName: String): Boolean {
    //
    // }
}
