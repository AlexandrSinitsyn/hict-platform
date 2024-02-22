package ru.itmo.hict.server.service

import io.minio.*
import io.minio.messages.Item
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import ru.itmo.hict.server.logging.Logger
import java.io.InputStream

@Configuration
class MinioConfiguration(
    @Value("\${minio.url}") private val url: String,
    @Value("\${minio.access.key}") private val accessKey: String,
    @Value("\${minio.access.secret}") private val secretKey: String,
) {
    @Bean
    fun minio(): MinioClient = MinioClient.builder()
        .endpoint(url)
        .credentials(accessKey, secretKey)
        .build()
}

@Service
class MinioService(
    private val minioClient: MinioClient,
    private val fileService: FileService,
    private val logger: Logger,
) {
    fun newBucketIfAbsent(name: String) {
        if (minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(name)
                .build())) {
            return
        }

        minioClient.makeBucket(MakeBucketArgs.builder()
            .bucket(name)
            .build())
    }

    private fun path(folder: String?, filename: String) = (folder?.let { "$it/" } ?: "") + filename

    class FileObjectInfo(val name: String,
                         val size: Long,
                         val data: InputStream)

    @Async
    fun upload(bucket: String, folder: String?, file: FileObjectInfo) {
        minioClient.putObject(PutObjectArgs.builder()
            .bucket(bucket)
            .`object`(path(folder, file.name))
            .stream(file.data, file.size, -1)
            .build())

        logger.info("uploading", "minio", "uploading completed `${path(folder, file.name)}`")
    }

    fun downloadFile(bucket: String, folder: String?, filename: String): FileObjectInfo {
        val size = minioClient.statObject(StatObjectArgs.builder()
            .bucket(bucket)
            .`object`(path(folder, filename))
            .build()).size()

        val input = minioClient.getObject(GetObjectArgs.builder()
            .bucket(bucket)
            .`object`(path(folder, filename))
            .build())

        logger.info("downloading", "minio", "downloading completed `${path(folder, filename)}`")

        return FileObjectInfo(filename, size, input)
    }

    fun listInBucket(bucket: String, folder: String?): List<Item> {
        return minioClient.listObjects(ListObjectsArgs.builder()
            .bucket(bucket)
            .recursive(true)
            .build()).map { it.get() }.filter { folder?.run { it.objectName().startsWith("$this/") } ?: true }
    }
}
