package ru.itmo.hict.server

import io.minio.MinioClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import ru.itmo.hict.server.logging.Logger
import ru.itmo.hict.server.service.FileService
import ru.itmo.hict.server.service.MinioService

class MinioServiceTests {
    private companion object {
        private val fileService = FileService()
        private lateinit var minioClient: MinioClient
        private lateinit var minioService: MinioService

        @JvmStatic
        @BeforeAll
        fun setup() {
            minioClient = mock<MinioClient>()
            val logger = mock<Logger>()
            doNothing().whenever(logger)

            minioService = MinioService(minioClient, fileService, logger)
        }
    }

    @Test
    fun `new bucket if absent`() {
        whenever(minioClient.bucketExists(any())) doReturn false
        doNothing().whenever(minioClient.makeBucket(any()))

        Assertions.assertDoesNotThrow {
            minioService.newBucketIfAbsent("new")
        }

        class TestException : RuntimeException()

        whenever(minioClient.bucketExists(any())) doReturn true
        whenever(minioClient.makeBucket(any())) doThrow TestException()

        Assertions.assertThrows(TestException::class.java) {
            minioService.newBucketIfAbsent("exists")
        }
    }

    @Test
    fun `upload existing`() {
        // doNothing().whenever(minioClient.putObject(any()))
        //
        // val tmpFile = fileService.minio("tmp.file")
        //
        // fileService.minio()

        // TODO
    }

    @Test
    fun `upload not existing`() {
        // TODO
    }

    @Test
    fun `upload folder`() {
        // TODO
    }

    @Test
    fun `download valid`() {
        // TODO
    }

    @Test
    fun `download invalid`() {
        // TODO
    }

    @Test
    fun `download folder`() {
        // TODO
    }

    @Test
    fun `download bucket`() {
        // TODO
    }

    @Test
    fun `list in bucket`() {
        // TODO
    }

    @Test
    fun `list in bucket (not existing)`() {
        // TODO
    }

    @Test
    fun `list in not a bucket`() {
        // TODO
    }
}
