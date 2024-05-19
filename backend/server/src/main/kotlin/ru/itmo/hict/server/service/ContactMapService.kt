package ru.itmo.hict.server.service

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import ru.itmo.hict.entity.ContactMap
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.repository.ContactMapRepository
import ru.itmo.hict.server.repository.ViewsRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class ContactMapService(
    private val contactMapRepository: ContactMapRepository,
    private val experimentService: ExperimentService,
    private val minioService: MinioService,
    private val viewsRepository: ViewsRepository,
) {
    @PostConstruct
    fun init() {
        listOf("hic", "agp", "mcool", "trakcs").forEach {
            minioService.newBucketIfAbsent(it)
        }
    }

    fun getByName(name: String): ContactMap? = contactMapRepository.findByName(name).getOrNull()

    fun create(experimentName: String): ContactMap? =
        experimentService.getByName(experimentName)?.run {
            contactMapRepository.save(ContactMap(UUID.randomUUID().toString(), this))
        }

    fun view(contactMap: ContactMap) = viewsRepository.viewById(contactMap.id!!)
}
