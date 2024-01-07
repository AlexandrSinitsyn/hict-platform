package ru.itmo.hict.server.service

import org.springframework.stereotype.Service
import ru.itmo.hict.entity.HiCMap
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.repository.HiCMapRepository
import kotlin.jvm.optionals.getOrNull

@Service
class HiCMapService(
    private val hiCMapRepository: HiCMapRepository,
) {
    fun getAll(): List<HiCMap> = hiCMapRepository.findAll()

    fun getById(id: Long): HiCMap? = hiCMapRepository.findById(id).getOrNull()

    fun checkUnique(name: String): Boolean = hiCMapRepository.findByName(name).isEmpty

    fun save(author: User, name: String, description: String): HiCMap =
        hiCMapRepository.save(HiCMap(author, name, description))
}
