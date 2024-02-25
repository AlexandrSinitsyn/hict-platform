package ru.itmo.hict.authorization.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import ru.itmo.hict.entity.User
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Query(
        value = """
            select * from new_user(:#{#user.username},
                                   :#{#user.login},
                                   :#{#user.email},
                                   :#{#user.password},
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
            where users.password = crypt(:password, users.password)
              and (users.login = :login or users.email = :email)
        """,
        nativeQuery = true,
    )
    fun findByLoginOrEmailAndPassword(@Param("login") login: String?,
                                         @Param("email") email: String?,
                                         @Param("password") password: String): Optional<User>
}
