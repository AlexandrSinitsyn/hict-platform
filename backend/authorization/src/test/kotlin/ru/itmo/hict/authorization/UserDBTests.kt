package ru.itmo.hict.authorization

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.itmo.hict.authorization.repositories.UserRepository
import ru.itmo.hict.entity.Role
import ru.itmo.hict.entity.User
import kotlin.jvm.optionals.getOrNull

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = ["ru.itmo.hict.entity"])
@Import(LiquibaseConfig::class)
@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UserDBTests {
    companion object {
        private const val DB_USER = "test"

        @JvmStatic
        @Container
        @ServiceConnection
        private val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:${System.getenv()["POSTGRES_VERSION"]}")
                .withUsername(DB_USER)
                .withFileSystemBind("src/test/resources/test-data",
                    "/test-data", BindMode.READ_ONLY)
    }

    @Autowired
    private lateinit var userRepository: UserRepository

    @Order(0)
    @Test
    fun contextLoads() {
        val execResult = postgres.execInContainer("psql", "-U", DB_USER, "-f", "/test-data/users.sql")

        Assertions.assertTrue(execResult.exitCode == 0)
        Assertions.assertTrue(execResult.stderr.isNullOrBlank())
    }

    @Test
    fun `check not empty database`() {
        Assertions.assertEquals(4, userRepository.count())
    }

    @Test
    fun `exists 'user' user`() {
        val user = userRepository.findByLoginOrEmail("user", "user@test.com").run {
            Assertions.assertNotNull(getOrNull())

            get()
        }

        Assertions.assertNotNull(user.id)
        Assertions.assertNotNull(user.creationTime)
        Assertions.assertEquals("user", user.username)
        Assertions.assertEquals("user", user.login)
        Assertions.assertEquals("user@test.com", user.email)
        Assertions.assertEquals(Role.USER, user.role)
    }

    @Test
    fun `same user on partial {login, email} info`() {
        val `login & email` = userRepository.findByLoginOrEmailAndPassword(
            "user", "user@test.com", "user").getOrNull()
        val `login` = userRepository.findByLoginOrEmailAndPassword(
            "user", null, "user").getOrNull()
        val `email` = userRepository.findByLoginOrEmailAndPassword(
            null, "user@test.com", "user").getOrNull()
        val `no info` = userRepository.findByLoginOrEmailAndPassword(
            null, null, "user").getOrNull()


        Assertions.assertNotNull(`login & email`)
        Assertions.assertNotNull(`login`)
        Assertions.assertNotNull(`email`)
        Assertions.assertNull(`no info`)

        Assertions.assertTrue(`login & email`!!.id == `login`!!.id && `login & email`.id == `email`!!.id)
        Assertions.assertTrue(`login & email`.creationTime == `login`.creationTime &&
                `login & email`.creationTime == `email`!!.creationTime)
    }

    @Test
    fun `new user`() {
        val user = User("username", "login", "email@test.com", "pass", Role.ANONYMOUS)

        val saved = userRepository.save(user).run {
            Assertions.assertTrue(this.isPresent)

            this.get()
        }

        Assertions.assertEquals(user.username, saved.username)
        Assertions.assertEquals(user.login, saved.login)
        Assertions.assertEquals(user.email, saved.email)
        Assertions.assertEquals(user.role, saved.role)

        Assertions.assertNotNull(saved.id)
        Assertions.assertNotNull(saved.creationTime)
    }

    @Test
    fun `no duplicate users`() {
        val duplicatedUser = userRepository.save(
            User("user", "user", "user@test.com", "user", Role.USER)).getOrNull()

        Assertions.assertNull(duplicatedUser)
    }
    
    @Test
    fun `different users`() {
        val first = userRepository.save(
            User("first", "first", "first@email.com", "first", Role.ANONYMOUS)).get()
        val second = userRepository.save(
            User("second", "second", "second@email.com", "second", Role.ANONYMOUS)).get()

        Assertions.assertNotEquals(second.id, first.id)
    }

    @Test
    fun `search for new user`() {
        val user = User("username", "login", "email@test.com", "pass", Role.ANONYMOUS)

        val saved = userRepository.save(user).run {
            Assertions.assertTrue(this.isPresent)

            this.get()
        }

        Assertions.assertEquals(user.username, saved.username)
        Assertions.assertEquals(user.login, saved.login)
        Assertions.assertEquals(user.email, saved.email)
        Assertions.assertEquals(user.role, saved.role)

        Assertions.assertNotNull(saved.id)
        Assertions.assertNotNull(saved.creationTime)
    }
}
