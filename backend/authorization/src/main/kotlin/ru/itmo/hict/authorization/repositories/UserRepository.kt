package ru.itmo.hict.authorization.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.itmo.hict.entity.User
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByLoginOrEmail(login: String?, email: String?): Optional<User>

    fun findByLoginOrEmailAndPasswordSha(login: String?, email: String?, passwordSha: String): Optional<User>
}
