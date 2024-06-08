package ru.itmo.hict.server

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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
import ru.itmo.hict.server.repository.UserRepository
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
        Assertions.assertEquals(3, userRepository.count())
    }

    @ParameterizedTest
    @ValueSource(strings = ["user", "test"])
    fun `find existing user`(name: String) {
        val user = userRepository.findByLoginAndPassword(name, name).run {
            Assertions.assertNotNull(getOrNull())

            get()
        }

        Assertions.assertNotNull(user.id)
        Assertions.assertNotNull(user.creationTime)
        Assertions.assertEquals(name, user.username)
        Assertions.assertEquals(name, user.login)
        Assertions.assertEquals("$name@test.com", user.email)
    }

    @Test
    fun `find anonymous user`() {
        Assertions.assertDoesNotThrow {
            val user = userRepository.findByLoginAndPassword("anonymous", "nopass").get()

            Assertions.assertNotNull(user.id)
            Assertions.assertNotNull(user.creationTime)
            Assertions.assertEquals("anonymous", user.username)
        }
    }

    @Test
    fun `check update email`() {
        val user = userRepository.findByLoginAndPassword("user", "user").get()

        Assertions.assertEquals("user@test.com", user.email)

        userRepository.updateEmail(user.id!!, "new@email.com")

        val newUser = userRepository.findByLoginAndPassword("user", "user").get()
        Assertions.assertEquals("new@email.com", newUser.email)

        userRepository.updateEmail(user.id!!, "user@test.com")

        val revertedUser = userRepository.findByLoginAndPassword("user", "user").get()
        Assertions.assertEquals("user@test.com", revertedUser.email)
    }

    @Test
    fun `check update password`() {
        val user = userRepository.findByLoginAndPassword("user", "user").get()

        userRepository.updatePassword(user.id!!, "user", "new-password")

        val newUser = userRepository.findByLoginAndPassword("user", "new-password").run {
            Assertions.assertNotNull(getOrNull())

            get()
        }
        Assertions.assertNotNull(newUser.id)
        Assertions.assertEquals(user.id, newUser.id)

        userRepository.findByLoginAndPassword("user", "user").run {
            Assertions.assertNull(getOrNull())
        }

        userRepository.updatePassword(user.id!!, "new-password", "user")

        val revertedUser = userRepository.findByLoginAndPassword("user", "user").run {
            Assertions.assertNotNull(getOrNull())

            get()
        }
        Assertions.assertNotNull(revertedUser.id)
        Assertions.assertEquals(user.id, revertedUser.id)

        userRepository.findByLoginAndPassword("user", "new-password").run {
            Assertions.assertNull(getOrNull())
        }
    }
}
