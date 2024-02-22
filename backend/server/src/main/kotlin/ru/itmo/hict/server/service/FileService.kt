package ru.itmo.hict.server.service

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import ru.itmo.hict.server.exception.InternalServerError
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class FileService {
    private val basePath = Paths.get(".")

    @PostConstruct
    fun init() {
        if (Files.notExists(tmp("."))) {
            Files.createDirectories(tmp("."))
        }

        if (Files.notExists(minio("."))) {
            Files.createDirectories(minio("."))
        }
    }

    fun tmp(path: String): Path = basePath.resolve("tmp").resolve(path)
    fun minio(path: String): Path = basePath.resolve("minio").resolve(path)

    fun save(input: InputStream, path: Path) = Files.copy(input, path)

    fun move(from: Path, to: Path) {
        if (Files.notExists(from)) {
            throw InternalServerError("File you are trying to move does not exist: $from")
        }

        Files.move(from, to)
    }
}
