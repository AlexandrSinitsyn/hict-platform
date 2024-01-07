package ru.itmo.hict.server.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itmo.hict.dto.HiCMapInfoDto
import ru.itmo.hict.dto.HiCMapInfoDto.Companion.toInfoDto
import ru.itmo.hict.server.service.HiCMapService

@RestController
@RequestMapping("/api/v1/hi-c")
class HiCMapController(
    private val hiCMapService: HiCMapService,
) {
    @GetMapping("/all")
    fun getAll(): ResponseEntity<List<HiCMapInfoDto>> =
        hiCMapService.getAll().map { it.toInfoDto() }.run { ResponseEntity.ok(this) }

    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: Long): ResponseEntity<HiCMapInfoDto> =
        hiCMapService.getById(id)?.run { ResponseEntity.ok(this.toInfoDto()) }
            ?: ResponseEntity.notFound().build()
}
