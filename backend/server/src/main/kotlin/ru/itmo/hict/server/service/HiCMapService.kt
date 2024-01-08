package ru.itmo.hict.server.service

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import ru.itmo.hict.entity.HiCMap
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.repository.HiCMapRepository
import java.io.File
import kotlin.jvm.optionals.getOrNull

@Service
class HiCMapService(
    private val hiCMapRepository: HiCMapRepository,
    private val minioService: MinioService,
) {
    private val bucket = "hi-c"

    @PostConstruct
    fun init() {
        minioService.newBucketIfAbsent(bucket)
    }

    fun getAll(): List<HiCMap> = hiCMapRepository.findAll()

    fun getById(id: Long): HiCMap? = hiCMapRepository.findById(id).getOrNull()

    fun checkUnique(name: String): Boolean = hiCMapRepository.findByName(name).isEmpty

    fun load(author: User, name: String, description: String, file: File): HiCMap {
        minioService.upload(bucket, "u${author.id}", file)

        return hiCMapRepository.save(HiCMap(author, name, description))
    }
}
