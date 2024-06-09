package ru.itmo.hict.server

import io.minio.MinioClient
import io.minio.errors.ErrorResponseException
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
import ru.itmo.hict.server.service.MinioService
import java.nio.file.AccessDeniedException
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

            minioService = MinioService(minioClient, logger)
        }
    }

    @Order(0)
    @Test
    fun `new bucket if absent`() {
        try {
            minioService.listInBucket(BUCKET)
            Assertions.assertTrue(false) { "Exception expected" }
        } catch (e: ErrorResponseException) {
            Assertions.assertTrue("bucket does not exist" in e.message!!)
        }

        Assertions.assertDoesNotThrow {
            minioService.newBucketIfAbsent(BUCKET)
        }

        Assertions.assertTrue(minioService.listInBucket(BUCKET).isEmpty())

        Assertions.assertDoesNotThrow {
            minioService.newBucketIfAbsent(BUCKET)
        }

        Assertions.assertTrue(minioService.listInBucket(BUCKET).isEmpty())
    }

    private fun Path.toFileObject() =
        MinioService.FileObjectInfo(fileName.pathString, toFile().length(), inputStream())

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = ["test-files/1.test", "test-files/2.test", "test-files/3.test"])
    fun `upload existing`(filepath: String) {
        val path = Path(RESOURCES, filepath)

        minioService.upload(BUCKET, path.toFileObject())

        Assertions.assertNotNull(minioService.listInBucket(BUCKET).find { "${path.fileName}" == it })
    }

    @Test
    fun `upload not existing`() {
        Assertions.assertThrows(NoSuchFileException::class.java) {
            minioService.upload(BUCKET, Path(RESOURCES, "unknown.file").toFileObject())
        }
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = ["test-files/1.test"])
    fun `download valid`(file: String) {
        Assertions.assertEquals(3, minioService.listInBucket(BUCKET).size)

        val filename = Path(file).name
        val downloaded = minioService.downloadFile(BUCKET, filename)

        Assertions.assertTrue(filename in downloaded.name)
        Assertions.assertTrue(downloaded.size > 0)
        Assertions.assertNotEquals(-1, downloaded.data.read())

        Assertions.assertEquals(3, minioService.listInBucket(BUCKET).size)
    }

    @Order(2)
    @Test
    fun `download invalid`() {
        Assertions.assertEquals(3, minioService.listInBucket(BUCKET).size)

        Assertions.assertThrows(ErrorResponseException::class.java) {
            minioService.downloadFile(BUCKET, "unknown.file")
        }

        Assertions.assertEquals(3, minioService.listInBucket(BUCKET).size)
    }

    @Test
    fun `download invalid bucket`() {
        Assertions.assertThrows(ErrorResponseException::class.java) {
            minioService.downloadFile("invalid", "existing")
        }
    }


    @Order(2)
    @Test
    fun `list in bucket`() {
        Assertions.assertEquals(3, minioService.listInBucket(BUCKET).size)
    }

    @Test
    fun `list in bucket (not existing)`() {
        Assertions.assertThrows(ErrorResponseException::class.java) {
            minioService.listInBucket("unknown")
        }
    }
}
