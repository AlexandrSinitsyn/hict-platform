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
    private val consoleCommandPrefix =
        if ("win" in System.getProperty("os.name").lowercase()) arrayOf("cmd.exe", "/C") else arrayOf()

    suspend fun runDocker(id: String): Result<Boolean> {
        val filepath = Path(id, FILENAME)

        withContext(Dispatchers.IO) {
            if (filepath.exists()) {
                return@withContext
            }

            filepath.createParentDirectories()

            filepath.bufferedWriter().use {
                it.write(DOCKER_COMPOSE.format(id, imageName, port))
                logger.info("process", "fileWriter", filepath.pathString)
            }
        }

        val result = runProcess(
            "docker", "compose",
            "-f", filepath.pathString,
            "-p", "hict-server-cluster",
            "up", "-d"
        )

        // withContext(Dispatchers.IO) {
        //     filepath.deleteExisting()
        // }

        logger.info("process", "cleanup", filepath.pathString)

        return result
    }

    suspend fun stopDocker(id: String): Result<Boolean> =
        runProcess("docker", "compose", "-f", Path(id, FILENAME).pathString, "down")

    private suspend fun runProcess(vararg command: String): Result<Boolean> = runCatching {
        val process = ProcessBuilder(*consoleCommandPrefix, *command).start()
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            throw IllegalStateException("Waiting time elapsed")
        }

        logger.info("commandRunner", "stated", command.joinToString(" "))

        if (process.exitValue() == 0) {
            return@runCatching true
        }

        throw IllegalStateException("""
            Command `${command.joinToString(" ")}` failed with ${process.exitValue()} code:
                stdout: ${process.inputReader(Charsets.UTF_8).use { it.readText() }}
                stderr: ${process.errorReader(Charsets.UTF_8).use { it.readText() }}
        """.trimIndent())
    }
}
