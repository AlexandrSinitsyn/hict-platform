package ru.itmo.hict.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import ru.itmo.hict.entity.HiCMap
import ru.itmo.hict.entity.Role
import ru.itmo.hict.entity.User
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    @Query(
        value = """
            select *
            from users
            where users.password = crypt(:password, users.password)
              and users.login = :login
        """,
        nativeQuery = true,
    )
    fun findByLoginAndPassword(@Param("login") login: String,
                               @Param("password") password: String): Optional<User>

    @Transactional
    @Modifying
    @Query("update User u set u.username = :username where u.id = :id")
    fun updateUsername(@Param("id") id: Long, @Param("username") username: String)

    @Transactional
    @Modifying
    @Query("update User u set u.login = :login where u.id = :id")
    fun updateLogin(@Param("id") id: Long, @Param("login") login: String)

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Modifying
    @Query("update User u set u.email = :email where u.id = :id")
    fun updateEmail(@Param("id") id: Long, @Param("email") email: String)

    @Transactional
    @Modifying
    @Query("update User u set u.role = :role where u.id = :id")
    fun updateRole(@Param("id") id: Long, @Param("role") role: Role)

    @Transactional
    @Modifying
    @Query(
        value = """
            update users
            set password = crypt(:new_password, gen_salt('bf'))
            where users.user_id = :id
              and users.password = crypt(:old_password, users.password)
        """,
        nativeQuery = true,
    )
    fun updatePassword(
        @Param("id") id: Long,
        @Param("old_password") oldPassword: String,
        @Param("new_password") newPassword: String,
    )
}

@Repository
interface HiCMapRepository : JpaRepository<HiCMap, Long> {
    // fixme
    @Transactional
    fun findByName(name: String): Optional<HiCMap>
}
