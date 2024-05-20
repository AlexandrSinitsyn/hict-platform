package ru.itmo.hict.server.service

import io.minio.*
import io.minio.messages.Item
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.entity.File
import ru.itmo.hict.entity.AttachedFile
import ru.itmo.hict.entity.Group
import ru.itmo.hict.entity.SequenceLevelType
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
    private val fileService: FileService,
    private val groupService: GroupService,
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

    fun load(fileType: FileType, filename: String, filesize: Long, data: InputStream): AttachedFile {
        val visibilityGroup = groupService.getByName("public")!!

        val saved = fileService.save(fileType, File(filename, SequenceLevelType.SCAFFOLD, filesize, visibilityGroup))

        upload(fileType.bucket, null, FileObjectInfo(filename, filesize, data))

        return saved
    }
}
