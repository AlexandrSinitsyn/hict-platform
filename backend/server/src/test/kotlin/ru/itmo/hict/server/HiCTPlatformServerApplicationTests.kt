package ru.itmo.hict.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.minio.MinioClient
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.*
import org.springframework.core.io.InputStreamResource
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.LinkedMultiValueMap
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.itmo.hict.dto.ContactMapInfoDto
import ru.itmo.hict.dto.Jwt
import ru.itmo.hict.dto.USER_ID_CLAIM
import ru.itmo.hict.entity.Experiment
import ru.itmo.hict.server.form.ContactMapCreationForm
import ru.itmo.hict.server.form.UpdatePasswordForm
import ru.itmo.hict.server.repository.ContactMapRepository
import ru.itmo.hict.server.repository.UserRepository
import ru.itmo.hict.server.service.MinioService
import ru.itmo.hict.server.service.S3Configuration
import java.net.URI
import java.time.Duration
import java.util.UUID

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = ["ru.itmo.hict.entity"])
@ActiveProfiles("full-app-test")
@ComponentScan("ru.itmo.hict.dto", "ru.itmo.hict.server")
// fixme excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [MinioConfiguration::class])])
@Import(LiquibaseConfig::class, HiCTPlatformServerApplicationTests.AppTestBeans::class)
@SpringBootTest(classes = [HiCTPlatformServerApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class HiCTPlatformServerApplicationTests {
    companion object {
        private const val NAME = "test"
        private const val DESCRIPTION = "Example description"
        private const val FILE_CONTENT = "Hello, World!"

        private const val DB_USER = "test"

        private fun env(name: String) = System.getenv()[name]

        @JvmStatic
        @Container
        @ServiceConnection
        private val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:${env("POSTGRES_VERSION")}")
                .withUsername(DB_USER)
                .withFileSystemBind("src/test/resources/test-data",
                    "/test-data", BindMode.READ_ONLY)

        @JvmStatic
        @Container
        private val minio: GenericContainer<*> =
            GenericContainer("minio/minio:${env("MINIO_VERSION")}")
                .withEnv("MINIO_ROOT_USER", env("MINIO_ROOT_USER"))
                .withEnv("MINIO_ROOT_PASSWORD", env("MINIO_ROOT_PASSWORD"))
                .withCommand("server /data")
                .withExposedPorts(9000)
                .waitingFor(HttpWaitStrategy()
                    .forPath("/minio/health/ready")
                    .forPort(9000)
                    .withStartupTimeout(Duration.ofSeconds(10)))
    }

    @MockBean
    private lateinit var s3Configuration: S3Configuration

    @TestConfiguration
    @Profile("full-app-test")
    class AppTestBeans {
        @Bean("testMinio")
        @Primary
        fun minio(): MinioClient = MinioClient.builder()
            .endpoint("http://localhost:${minio.firstMappedPort}")
            .credentials(env("MINIO_ROOT_USER"), env("MINIO_ROOT_PASSWORD"))
            .build()
    }

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var algorithm: Algorithm

    @Autowired
    private lateinit var contactMapRepository: ContactMapRepository

    @Autowired
    private lateinit var minioService: MinioService

    @LocalServerPort
    protected var randomPort: Int = 0

    private val server: String
        get() = "http://localhost:$randomPort/api/v1"

    private val userPass = "user"
    private val userId by lazy { userRepository.findByLoginAndPassword("user", userPass).get().id!! }

    private val jwt by lazy { JWT.create().withClaim(USER_ID_CLAIM, "$userId").sign(algorithm) }

    @Order(0)
	@Test
	fun contextLoads() {
        val execResult = postgres.execInContainer("psql", "-U", DB_USER, "-f", "/test-data/users.sql")

        Assertions.assertTrue(execResult.exitCode == 0)
        Assertions.assertTrue(execResult.stderr.isNullOrBlank())
	}

    @AfterEach
    fun clearDb() {
        contactMapRepository.deleteAll()
    }

    @Test
    fun `search for nothing`() {
        restTemplate.getForEntity<List<ContactMapInfoDto>>(URI("$server/hi-c/all")).run {
            Assertions.assertTrue(statusCode.is2xxSuccessful)
            Assertions.assertNotNull(body)
            Assertions.assertTrue(body!!.isEmpty())
        }

        restTemplate.getForEntity<List<ContactMapInfoDto>>(URI("$server/hi-c/acquire/unknown")).run {
            Assertions.assertTrue(statusCode.is4xxClientError)
            Assertions.assertNull(body)
        }
    }

    private fun file(content: String) = object : InputStreamResource(content.byteInputStream()) {
        override fun getFilename() = "$NAME.hic"

        override fun contentLength() = -1L
    }

    private inline fun <reified T> publish(content: String?, jwt: Jwt?): ResponseEntity<T> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        jwt?.let { headers.setBearerAuth(it) }

        val body = LinkedMultiValueMap<String, Any>()
        body.add("file", file(content ?: ""))
        body.add("form", ContactMapCreationForm(UUID.randomUUID()))

        return restTemplate.postForEntity(
            URI("$server/hi-c/publish"),
            HttpEntity(body, headers),
            T::class.java
        )
    }

    @Test
    fun `save one Hi-C map`() {
        Assertions.assertEquals(0, minioService.listInBucket("hi-c").size)

        publish<ContactMapInfoDto>(FILE_CONTENT, jwt).run {
            Assertions.assertTrue(statusCode.is2xxSuccessful)
            Assertions.assertNotNull(body)
        }

        Assertions.assertEquals(1, minioService.listInBucket("hi-c").size)
        Assertions.assertTrue(minioService.listInBucket("hi-c")[0].startsWith("u$userId"))
        Assertions.assertEquals(1, minioService.listInBucket("hi-c").size)
        Assertions.assertEquals("u$userId/$NAME.hic", minioService.listInBucket("hi-c")[0])
    }

    @Test
    fun `save already present`() {
        publish<String>(FILE_CONTENT, jwt)

        publish<String>(FILE_CONTENT, jwt).run {
            Assertions.assertTrue(statusCode.is4xxClientError)
            Assertions.assertNotNull(body)
            Assertions.assertTrue(body!!.isNotBlank())
            Assertions.assertTrue("must be unique" in body!!)
        }
    }

    @Test
    fun `save invalid`() {
        publish<String>(null, jwt).run {
            Assertions.assertTrue(statusCode.is4xxClientError)
            Assertions.assertNotNull(body)
            Assertions.assertTrue(body!!.isNotBlank())
            Assertions.assertTrue("should not be empty" in body!!)
        }
    }

    @Test
    fun `no JWT (publish)`() {
        publish<String>(FILE_CONTENT, null).run {
            Assertions.assertTrue(statusCode.is4xxClientError)
            Assertions.assertNotNull(body)
            Assertions.assertTrue(body!!.isNotBlank())
            Assertions.assertTrue("must be authorized" in body!!)
        }
    }

    @Test
    fun `invalid JWT (publish)`() {
        publish<String>(FILE_CONTENT, "invalid").run {
            Assertions.assertTrue(statusCode.is4xxClientError)
            Assertions.assertNotNull(body)
            Assertions.assertTrue(body!!.isNotBlank())
            Assertions.assertTrue("invalid jwt" in body!!.lowercase())
        }
    }

    private inline fun <reified T> updatePassword(oldPassword: String, newPassword: String, jwt: Jwt?): ResponseEntity<T> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        jwt?.let { headers.setBearerAuth(it) }

        return restTemplate.exchange(
            URI("$server/users/update/password"),
            HttpMethod.PATCH,
            HttpEntity(UpdatePasswordForm(oldPassword, newPassword), headers),
            T::class.java
        )
    }

    @Test
    fun `successful change password`() {
        val newPass = "newPass"

        updatePassword<Boolean>(userPass, newPass, jwt).run {
            Assertions.assertTrue(statusCode.is2xxSuccessful)
            Assertions.assertNotNull(body)
            Assertions.assertTrue(body!!)
        }

        updatePassword<Boolean>(newPass, userPass, jwt).run {
            Assertions.assertTrue(statusCode.is2xxSuccessful)
            Assertions.assertNotNull(body)
            Assertions.assertTrue(body!!)
        }
    }

    @Test
    fun `same password change password`() {
        updatePassword<String>(userPass, userPass, jwt).run {
            Assertions.assertTrue(statusCode.is4xxClientError)
            Assertions.assertNotNull(body)
            Assertions.assertTrue(body!!.isNotBlank())
            Assertions.assertTrue("should be different" in body!!.lowercase())
        }
    }

    @Test
    fun `invalid password change password`() {
        updatePassword<String>("invalid", "newPass", jwt).run {
            Assertions.assertTrue(statusCode.is4xxClientError)
            Assertions.assertNotNull(body)
            Assertions.assertTrue(body!!.isNotBlank())
            Assertions.assertTrue("must confirm" in body!!.lowercase())
        }
    }

    @Test
    fun `no JWT (update password)`() {
        updatePassword<String>(userPass, "newPass", null).run {
            Assertions.assertTrue(statusCode.is4xxClientError)
            Assertions.assertNotNull(body)
            Assertions.assertTrue(body!!.isNotBlank())
            Assertions.assertTrue("must be authorized" in body!!.lowercase())
        }
    }

    @Test
    fun `invalid JWT (update password)`() {
        updatePassword<String>(userPass, "newPass", "invalid").run {
            Assertions.assertTrue(statusCode.is4xxClientError)
            Assertions.assertNotNull(body)
            Assertions.assertTrue(body!!.isNotBlank())
            Assertions.assertTrue("invalid jwt" in body!!.lowercase())
        }
    }
}
