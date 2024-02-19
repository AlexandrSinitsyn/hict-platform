package ru.itmo.hict.server.repository

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.itmo.hict.entity.HiCMap
import ru.itmo.hict.entity.User
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long>

@Repository
interface HiCMapRepository : JpaRepository<HiCMap, Long> {
    // fixme
    @Transactional
    fun findByName(name: String): Optional<HiCMap>
}
