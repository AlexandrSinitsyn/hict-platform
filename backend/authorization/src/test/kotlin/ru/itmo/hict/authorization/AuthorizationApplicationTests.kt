package ru.itmo.hict.authorization

import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.http.ResponseEntity
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.itmo.hict.authorization.form.EnterForm
import ru.itmo.hict.authorization.form.RegisterForm
import ru.itmo.hict.authorization.repository.UserRepository
import ru.itmo.hict.dto.Jwt
import java.net.URI

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = ["ru.itmo.hict.entity"])
@ComponentScan("ru.itmo.hict.dto", "ru.itmo.hict.authorization")
@Import(LiquibaseConfig::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthorizationApplicationTests {
    companion object {
        @JvmStatic
        @Container
        @ServiceConnection
        private val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:${System.getenv()["POSTGRES_VERSION"]}")
    }

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var userRepository: UserRepository

    @LocalServerPort
    protected var randomPort: Int = 0

    private val server: String
        get() = "http://localhost:$randomPort"

    private fun register(username: String, login: String, email: String, password: String): ResponseEntity<Jwt> {
        val request = RegisterForm(username, login, email, password)

        return restTemplate.postForEntity(URI("$server/api/v1/auth/register"), request, Jwt::class.java)
    }

    private fun login(login: String?, email: String?, password: String): ResponseEntity<Jwt> {
        val request = EnterForm(login, email, password)

        return restTemplate.postForEntity(URI("$server/api/v1/auth/login"), request, Jwt::class.java)
    }

	@Test
	fun contextLoads() {
	}

    @AfterEach
    fun clearDb() {
        userRepository.deleteAll()
    }

    @Test
    fun `register user`() {
        val response = register("username", "login", "email@test.com", "password")

        Assertions.assertTrue(response.statusCode.is2xxSuccessful)
        Assertions.assertNotNull(response.body)
        Assertions.assertFalse(response.body.isNullOrBlank())
    }

    @Test
    fun `single user`() {
        Assertions.assertEquals(0, userRepository.count())

        register("username", "login", "email@test.com", "password")

        Assertions.assertEquals(1, userRepository.count())
    }

    @Test
    fun `login user`() {
        val registered = register("username", "login", "email@test.com", "password").body

        val loggedIn = login("login", "email@test.com", "password").body

        Assertions.assertNotNull(loggedIn)
        Assertions.assertNotNull(registered)
        Assertions.assertEquals(loggedIn, registered)
    }

    @Test
    fun `invalid register`() {
        Assertions.assertDoesNotThrow {
            val response = register("    ", "", "invalid", "1")

            Assertions.assertTrue(response.statusCode.is4xxClientError)
            Assertions.assertNotNull(response.body)
            Assertions.assertFalse(response.body.isNullOrBlank())
            val body = response.body!!
            Assertions.assertTrue(
                "blank" in body || "size must be between" in body || "must be a well-formed email" in body
            )
        }
    }

    @Test
    fun `invalid login`() {
        Assertions.assertDoesNotThrow {
            val response = login("unknown", "unknown@email.com", "invalid")

            Assertions.assertTrue(response.statusCode.is4xxClientError)
            Assertions.assertNotNull(response.body)
            Assertions.assertFalse(response.body.isNullOrBlank())
            Assertions.assertTrue(response.body!!.contains("Invalid login or password"))
        }
    }
}
