package ru.itmo.hict.scheduler.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils
import java.util.concurrent.TimeUnit
import kotlin.io.path.*

@Service
class DindService(
    @Value("\${HICT_SERVER_IMAGE}") private val imageName: String,
    @Value("\${HICT_SERVER_PORT}") private val port: String,
) {
    private val FILENAME = "docker-compose.yml"
    private val DOCKER_COMPOSE =
        ResourceUtils.getFile("classpath:" + "docker-compose.hict-server.yml").readText()

    suspend fun runDocker(id: String): Result<Boolean> {
        val filepath = Path(id, FILENAME)

        withContext(Dispatchers.IO) {
            filepath.createParentDirectories()

            filepath.bufferedWriter().use {
                it.write(DOCKER_COMPOSE.format(id, imageName, port))
            }
        }

        val result = runProcess("docker", "compose", "-f", filepath.pathString, "-p", "hict-server-cluster", "up", "-d")

        withContext(Dispatchers.IO) {
            filepath.deleteExisting()
        }

        return result
    }

    private suspend fun runProcess(vararg command: String): Result<Boolean> = runCatching {
        val process = ProcessBuilder(*command).start()
        process.waitFor(5, TimeUnit.SECONDS)

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
