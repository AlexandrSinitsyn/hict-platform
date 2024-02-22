package ru.itmo.hict.server

import io.minio.MinioClient
import io.minio.errors.ErrorResponseException
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.*
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.itmo.hict.server.logging.Logger
import ru.itmo.hict.server.service.FileService
import ru.itmo.hict.server.service.MinioService
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.*

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class MinioServiceTests {
    private companion object {
        private const val RESOURCES = "src/test/resources"
        private const val BUCKET = "test"
        private val fileService = FileService().apply {
            init()
        }
        private lateinit var minioService: MinioService

        private val accessKey by lazy { (0..10).map { ('a'..'z').random() }.joinToString("") }
        private val secretKey by lazy { (0..10).map { ('a'..'z').random() }.joinToString("") }

        @JvmStatic
        @Container
        private val minio: GenericContainer<*> =
            GenericContainer("minio/minio:${System.getenv()["MINIO_VERSION"]}")
                .withEnv("MINIO_ROOT_USER", accessKey)
                .withEnv("MINIO_ROOT_PASSWORD", secretKey)
                .withCommand("server /data")
                .withExposedPorts(9000)
                .waitingFor(
                    HttpWaitStrategy()
                    .forPath("/minio/health/ready")
                    .forPort(9000)
                    .withStartupTimeout(Duration.ofSeconds(10)))

        @JvmStatic
        @BeforeAll
        fun setup() {
            val minioClient = MinioClient.builder()
                .endpoint("http://localhost:${minio.firstMappedPort}")
                .credentials(accessKey, secretKey)
                .build()
            val logger = mock<Logger>()
            doNothing().whenever(logger)

            minioService = MinioService(minioClient, fileService, logger)
        }

        @JvmStatic
        @BeforeAll
        fun init() {
            Assertions.assertTrue(Files.list(fileService.tmp(".")).count() == 0L)
            Assertions.assertTrue(Files.list(fileService.minio(".")).count() == 0L)
        }

        @JvmStatic
        @AfterAll
        fun destructor() {
            Assertions.assertTrue(Files.list(fileService.tmp(".")).count() == 0L)
            Assertions.assertTrue(Files.list(fileService.minio(".")).count() == 0L)
        }
    }

    @Order(0)
    @Test
    fun `new bucket if absent`() {
        try {
            minioService.listInBucket(BUCKET, null)
            Assertions.assertTrue(false) { "Exception expected" }
        } catch (e: ErrorResponseException) {
            Assertions.assertTrue("bucket does not exist" in e.message!!)
        }

        Assertions.assertDoesNotThrow {
            minioService.newBucketIfAbsent(BUCKET)
        }

        Assertions.assertTrue(minioService.listInBucket(BUCKET, null).isEmpty())

        Assertions.assertDoesNotThrow {
            minioService.newBucketIfAbsent(BUCKET)
        }

        Assertions.assertTrue(minioService.listInBucket(BUCKET, null).isEmpty())
    }

    private fun Path.toFileObject() =
        MinioService.FileObjectInfo(fileName.pathString, toFile().length(), inputStream())

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = ["test-files/1.test", "test-files/2.test", "test-files/3.test"])
    fun `upload existing`(filepath: String) {
        val path = Path(RESOURCES, filepath)

        minioService.upload(BUCKET, "existing", path.toFileObject())

        Assertions.assertNotNull(minioService.listInBucket(BUCKET, "existing")
            .find { "existing/${path.fileName}" == it.objectName() })
    }

    @Test
    fun `upload not existing`() {
        Assertions.assertThrows(NoSuchFileException::class.java) {
            minioService.upload(BUCKET, "not-existing", Path(RESOURCES, "unknown.file").toFileObject())
        }
    }

    @Test
    fun `upload folder`() {
        Assertions.assertThrows(AccessDeniedException::class.java) {
            minioService.upload(BUCKET, "folder", Path(RESOURCES, "test-files").toFileObject())
        }
    }

    @Order(2)
    @Test
    fun `download valid`() {
        Assertions.assertEquals(3, minioService.listInBucket(BUCKET, "existing").size)

        val downloaded = minioService.downloadFile(BUCKET, "existing", "1.test")

        Assertions.assertTrue("1.test" in downloaded.name)
        Assertions.assertTrue(downloaded.size > 0)
        Assertions.assertNotEquals(-1, downloaded.data.read())

        Assertions.assertEquals(3, minioService.listInBucket(BUCKET, "existing").size)
    }

    @Order(2)
    @Test
    fun `download invalid`() {
        Assertions.assertTrue(minioService.listInBucket(BUCKET, "not-existing").isEmpty())

        Assertions.assertThrows(ErrorResponseException::class.java) {
            minioService.downloadFile(BUCKET, "not-existing", "unknown.file")
        }

        Assertions.assertTrue(minioService.listInBucket(BUCKET, "not-existing").isEmpty())
    }

    @Test
    fun `download folder`() {
        Assertions.assertThrows(ErrorResponseException::class.java) {
            minioService.downloadFile(BUCKET, null, "existing")
        }
    }

    @Test
    fun `download bucket`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            minioService.downloadFile(BUCKET, null, "")
        }
    }

    @Order(2)
    @Test
    fun `list in bucket`() {
        Assertions.assertEquals(3, minioService.listInBucket(BUCKET, null).size)
    }

    @Test
    fun `list in bucket (not existing)`() {
        Assertions.assertThrows(ErrorResponseException::class.java) {
            minioService.listInBucket("unknown", "")
        }
    }

    @Test
    fun `list in not a bucket`() {
        Assertions.assertEquals(3, minioService.listInBucket(BUCKET, "existing").size)

        Assertions.assertTrue(minioService.listInBucket(BUCKET, "").isEmpty())

        Assertions.assertTrue(minioService.listInBucket(BUCKET, "not-existing").isEmpty())
    }
}
