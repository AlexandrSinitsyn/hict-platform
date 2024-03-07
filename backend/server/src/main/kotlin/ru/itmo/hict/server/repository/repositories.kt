package ru.itmo.hict.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import ru.itmo.hict.entity.*
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

    @Query(
        value = """
            select u
            from User u
            where (:login is not null and u.login = :login)
               or (:email is not null and u.email = :email)
        """,
    )
    fun findByLoginOrEmail(@Param("login") login: String?, @Param("email") email: String?): Optional<User>

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Modifying(clearAutomatically = true)
    @Query("update User u set u.username = :username where u.id = :id")
    fun updateUsername(@Param("id") id: Long, @Param("username") username: String)

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying(clearAutomatically = true)
    @Query("update User u set u.login = :login where u.id = :id")
    fun updateLogin(@Param("id") id: Long, @Param("login") login: String)

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying(clearAutomatically = true)
    @Query("update User u set u.email = :email where u.id = :id")
    fun updateEmail(@Param("id") id: Long, @Param("email") email: String)

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying(clearAutomatically = true)
    @Query("update User u set u.role = :role where u.id = :id")
    fun updateRole(@Param("id") id: Long, @Param("role") role: Role)

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying(flushAutomatically = true)
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
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun findByName(name: String): Optional<HiCMap>
}

@Repository
interface ViewsRepository : JpaRepository<Views, Long> {
    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Modifying
    @Query(
        value = """
            merge into hi_c_map_views
            using (select :id as id) new
            on hi_c_map_views.hi_c_map_id = new.id
            when matched then update set hi_c_map_views.count = hi_c_map_views.count + 1
            when not matched then insert (hi_c_map_id, count) values (new.id, 1)
        """,
        nativeQuery = true,
    )
    fun viewById(@Param("id") hiCMapId: Long)
}
