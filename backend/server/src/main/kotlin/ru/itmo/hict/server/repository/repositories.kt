package ru.itmo.hict.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import ru.itmo.hict.entity.*
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
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
    fun updateUsername(@Param("id") id: UUID, @Param("username") username: String)

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying(clearAutomatically = true)
    @Query("update User u set u.login = :login where u.id = :id")
    fun updateLogin(@Param("id") id: UUID, @Param("login") login: String)

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying(clearAutomatically = true)
    @Query("update User u set u.email = :email where u.id = :id")
    fun updateEmail(@Param("id") id: UUID, @Param("email") email: String)

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
        @Param("id") id: UUID,
        @Param("old_password") oldPassword: String,
        @Param("new_password") newPassword: String,
    )
}

@Repository
interface GroupRepository : JpaRepository<Group, UUID> {
    fun getByName(name: String): Optional<Group>

    @Query("select count(g.id) > 0 from Group g where g.name = :name")
    fun existsByName(@Param("name") name: String): Boolean

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying
    @Query(value = "update Group g set g.name = :newName where g.name = :name")
    fun updateName(@Param("name") name: String, @Param("newName") newName: String)

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying
    @Query(
        value = """
            insert into user_groups (user_id, group_id)
            values (:#{#user.id}, (select groups.group_id
                                   from groups
                                   where groups.group_name = :name))
        """,
        nativeQuery = true,
    )
    fun join(@Param("name") name: String, @Param("user") user: User)
}

@Repository
interface ExperimentRepository : JpaRepository<Experiment, UUID> {
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun findByName(name: String): Optional<Experiment>

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying(flushAutomatically = true)
    @Query(
        value = """
            update experiments
            set experiment_name = :name
            where experiments.experiment_id = :#{#experiment.id}
        """,
        nativeQuery = true,
    )
    fun updateName(@Param("experiment") experiment: Experiment, @Param("name") name: String)

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying(flushAutomatically = true)
    @Query(
        value = """
            update experiments
            set description = :description,
                paper = :paper,
                attribution = :attribution
            where experiments.experiment_id = :#{#experiment.id}
        """,
        nativeQuery = true,
    )
    fun updateInfo(
        @Param("experiment") experiment: Experiment,
        @Param("description") name: String?,
        @Param("paper") paper: String?,
        @Param("attribution") attribution: String?,
    )
}

@Repository
interface ContactMapRepository : JpaRepository<ContactMap, UUID> {
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun findByName(name: String): Optional<ContactMap>

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying(flushAutomatically = true)
    @Query(
        value = """
            update contact_maps
            set contact_map_name = :name
            where contact_maps.contact_map_id = :#{#contactMap.id}
        """,
        nativeQuery = true,
    )
    fun updateName(@Param("contactMap") contactMap: ContactMap, @Param("name") name: String)

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Modifying(flushAutomatically = true)
    @Query(
        value = """
            update contact_maps
            set description = :description,
                hic_data_link = :link
            where contact_maps.contact_map_id = :#{#contactMap.id}
        """,
        nativeQuery = true,
    )
    fun updateInfo(
        @Param("contactMap") contactMap: ContactMap,
        @Param("description") name: String?,
        @Param("link") link: String?,
    )
}

@Repository
interface ViewsRepository : JpaRepository<ContactMapViews, UUID> {
    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Modifying
    @Query(
        value = """
            merge into hi_c_map_views
            using (select :id as id) as new
            on hi_c_map_views.hi_c_map_id = new.id
            when matched then
                update set count = count + 1
            when not matched then
                insert (hi_c_map_id, count) values (new.id, 1)
        """,
        nativeQuery = true,
    )
    fun viewById(@Param("id") hiCMapId: UUID)
}
