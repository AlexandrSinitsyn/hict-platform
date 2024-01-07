package ru.itmo.hict.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.itmo.hict.entity.HiCMap

@Repository
interface HiCMapRepository : JpaRepository<HiCMap, Long>
