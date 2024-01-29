package ru.itmo.hict.authorization.repositories

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.itmo.hict.entity.User
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    @Transactional
    @Query(
        value = """
            select * from new_user(:#{#user.username},
                                   :#{#user.login},
                                   :#{#user.email},
                                   :#{#user.passwordSha},
                                   cast(:#{#user.role.ordinal()} as smallint))
        """,
        nativeQuery = true,
    )
    fun save(@Param("user") user: User): Optional<User>

    fun findByLoginOrEmail(login: String?, email: String?): Optional<User>

    @Query(
        value = """
            select *
            from users
            where users.password_sha = crypt(:password, users.salt)
              and (users.login = :login or users.email = :email)
        """,
        nativeQuery = true,
    )
    fun findByLoginOrEmailAndPasswordSha(@Param("login") login: String?,
                                         @Param("email") email: String?,
                                         @Param("password") passwordSha: String): Optional<User>
}
