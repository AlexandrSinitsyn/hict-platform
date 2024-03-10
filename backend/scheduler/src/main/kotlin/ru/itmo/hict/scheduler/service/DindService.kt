package ru.itmo.hict.scheduler.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import ru.itmo.hict.scheduler.logging.Logger
import java.util.concurrent.TimeUnit
import kotlin.io.path.*

@Service
class DindService(
    resourceLoader: ResourceLoader,
    @Value("\${HICT_SERVER_IMAGE}") private val imageName: String,
    @Value("\${HICT_SERVER_PORT}") private val port: String,
    private val logger: Logger,
) {
    private val FILENAME = "docker-compose.yml"
    private val DOCKER_COMPOSE = resourceLoader.getResource("classpath:" +
            "docker-compose.hict-server.yml").getContentAsString(Charsets.UTF_8)

    suspend fun runDocker(id: String): Result<Boolean> {
        val filepath = Path(id, FILENAME)

        withContext(Dispatchers.IO) {
            filepath.createParentDirectories()

            filepath.bufferedWriter().use {
                it.write(DOCKER_COMPOSE.format(id, imageName, port))
            }
        }

        logger.info("process", "fileWriter", filepath.pathString)

        val result = runProcess("docker", "compose", "-f", filepath.pathString, "-p", "hict-server-cluster", "up", "-d")

        withContext(Dispatchers.IO) {
            filepath.deleteExisting()
        }

        logger.info("process", "cleanup", filepath.pathString)

        return result
    }

    private suspend fun runProcess(vararg command: String): Result<Boolean> = runCatching {
        val process = ProcessBuilder(*command).start()
        process.waitFor(5, TimeUnit.SECONDS)

        logger.info("commandRunner", "stated", command.joinToString(" "))

        if (process.exitValue() == 0) {
            return@runCatching true
        }

        throw IllegalStateException("""
            Command `${command.joinToString(" ")}` failed with ${process.exitValue()} code:
                stdout: ${process.inputReader().use { it.readText() }}
                stderr: ${process.errorReader().use { it.readText() }}
        """.trimIndent())
    }
}
