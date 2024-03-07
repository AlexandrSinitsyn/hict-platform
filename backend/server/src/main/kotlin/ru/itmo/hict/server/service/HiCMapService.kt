package ru.itmo.hict.server.service

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import ru.itmo.hict.entity.HiCMap
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.repository.HiCMapRepository
import ru.itmo.hict.server.repository.ViewsRepository
import kotlin.jvm.optionals.getOrNull

@Service
class HiCMapService(
    private val hiCMapRepository: HiCMapRepository,
    private val minioService: MinioService,
    private val viewsRepository: ViewsRepository,
) {
    private val bucket = "hi-c"

    @PostConstruct
    fun init() {
        minioService.newBucketIfAbsent(bucket)
    }

    fun getAll(): List<HiCMap> = hiCMapRepository.findAll()

    fun getByName(name: String): HiCMap? = hiCMapRepository.findByName(name).getOrNull()

    fun view(hiCMap: HiCMap) = viewsRepository.viewById(hiCMap.id!!)

    fun checkUnique(name: String): Boolean = hiCMapRepository.findByName(name).isEmpty

    fun load(author: User, name: String, description: String, data: MinioService.FileObjectInfo): HiCMap {
        minioService.upload(bucket, "u${author.id}", data)

        return hiCMapRepository.save(HiCMap(author, name, description))
    }
}
