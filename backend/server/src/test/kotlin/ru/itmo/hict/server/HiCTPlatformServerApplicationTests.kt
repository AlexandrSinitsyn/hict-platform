package ru.itmo.hict.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.minio.MinioClient
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
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
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.LinkedMultiValueMap
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.itmo.hict.dto.HiCMapInfoDto
import ru.itmo.hict.dto.Jwt
import ru.itmo.hict.dto.USER_ID_CLAIM
import ru.itmo.hict.server.form.HiCMapCreationForm
import ru.itmo.hict.server.repository.HiCMapRepository
import ru.itmo.hict.server.repository.UserRepository
import ru.itmo.hict.server.service.MinioConfiguration
import ru.itmo.hict.server.service.MinioService
import java.net.URI
import java.time.Duration

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
    private lateinit var miniConfiguration: MinioConfiguration

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
    private lateinit var hiCMapRepository: HiCMapRepository

    @Autowired
    private lateinit var minioService: MinioService

    @LocalServerPort
    protected var randomPort: Int = 0

    private val server: String
        get() = "http://localhost:$randomPort/api/v1/hi-c"

    private val userId by lazy { userRepository.findByLoginAndPassword("user", "user").get().id!! }

    private val jwt by lazy { JWT.create().withClaim(USER_ID_CLAIM, userId).sign(algorithm) }

    @Order(0)
	@Test
	fun contextLoads() {
        val execResult = postgres.execInContainer("psql", "-U", DB_USER, "-f", "/test-data/users.sql")

        Assertions.assertTrue(execResult.exitCode == 0)
        Assertions.assertTrue(execResult.stderr.isNullOrBlank())
	}

    @AfterEach
    fun clearDb() {
        hiCMapRepository.deleteAll()
    }

    @Test
    fun `search for nothing`() {
        restTemplate.getForEntity<List<HiCMapInfoDto>>(URI("$server/all")).run {
            Assertions.assertTrue(statusCode.is2xxSuccessful)
            Assertions.assertNotNull(body)
            Assertions.assertTrue(body!!.isEmpty())
        }

        restTemplate.getForEntity<List<HiCMapInfoDto>>(URI("$server/0")).run {
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
        body.add("form", HiCMapCreationForm(NAME, DESCRIPTION))

        return restTemplate.postForEntity(
            URI("$server/publish"),
            HttpEntity(body, headers),
            T::class.java
        )
    }

    @Test
    fun `save one Hi-C map`() {
        Assertions.assertEquals(0, minioService.listInBucket("hi-c", null).size)

        publish<HiCMapInfoDto>(FILE_CONTENT, jwt).run {
            Assertions.assertTrue(statusCode.is2xxSuccessful)
            Assertions.assertNotNull(body)
            Assertions.assertEquals(body!!.author.id, userId)
        }

        Assertions.assertEquals(1, minioService.listInBucket("hi-c", null).size)
        Assertions.assertTrue(minioService.listInBucket("hi-c", null)[0].objectName().startsWith("u$userId"))
        Assertions.assertEquals(1, minioService.listInBucket("hi-c", "u$userId").size)
        Assertions.assertEquals("u$userId/$NAME.hic", minioService.listInBucket("hi-c", "u$userId")[0].objectName())
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
    fun `no JWT`() {
        publish<String>(FILE_CONTENT, null).run {
            Assertions.assertTrue(statusCode.is4xxClientError)
            Assertions.assertNotNull(body)
            Assertions.assertTrue(body!!.isNotBlank())
            Assertions.assertTrue("must be authorized" in body!!)
        }
    }

    @Test
    fun `invalid JWT`() {
        publish<String>(FILE_CONTENT, "invalid").run {
            Assertions.assertTrue(statusCode.is4xxClientError)
            Assertions.assertNotNull(body)
            Assertions.assertTrue(body!!.isNotBlank())
            Assertions.assertTrue("invalid jwt" in body!!.lowercase())
        }
    }
}
