package ru.itmo.hict.server.service

import io.minio.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.server.logging.Logger
import java.io.InputStream

@Configuration
class S3Configuration(
    @Value("\${s3.url}") private val url: String,
    @Value("\${s3.access.key}") private val accessKey: String,
    @Value("\${s3.access.secret}") private val secretKey: String,
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

    class FileObjectInfo(val name: String,
                         val size: Long,
                         val data: InputStream)

    @Async
    fun upload(bucket: String, file: FileObjectInfo) {
        minioClient.putObject(PutObjectArgs.builder()
            .bucket(bucket)
            .`object`(file.name)
            .stream(file.data, file.size, -1)
            .build())

        logger.info("uploading", "minio", "uploading completed `${file.name}`")
    }

    fun downloadFile(bucket: String, filename: String): FileObjectInfo {
        val size = minioClient.statObject(StatObjectArgs.builder()
            .bucket(bucket)
            .`object`(filename)
            .build()).size()

        val input = minioClient.getObject(GetObjectArgs.builder()
            .bucket(bucket)
            .`object`(filename)
            .build())

        logger.info("downloading", "minio", "downloading completed `${filename}`")

        return FileObjectInfo(filename, size, input)
    }

    fun listInBucket(bucket: String): List<String> {
        return minioClient.listObjects(ListObjectsArgs.builder()
            .bucket(bucket)
            .recursive(true)
            .build()).map { it.get().objectName() }
    }

    @Async
    fun upload(fileType: FileType, filename: String, filesize: Long, data: InputStream) =
        upload(fileType.bucket, FileObjectInfo(filename, filesize, data))
}
