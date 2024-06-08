package ru.itmo.hict.scheduler

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.core.io.DefaultResourceLoader
import ru.itmo.hict.scheduler.logging.Logger
import ru.itmo.hict.scheduler.service.DindService
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

class CommandRunnerTests {
    private companion object {
        private lateinit var runProcess: (command: Array<String>) -> Result<Boolean>

        @JvmStatic
        @BeforeAll
        fun init() {
            val logger = mock<Logger>()
            val dindService = DindService(DefaultResourceLoader(), "unknown", "0", logger)
            val method = dindService::class.declaredMemberFunctions.find { it.name == "runProcess" }?.apply {
                isAccessible = true
            } ?: throw IllegalStateException("Not found `runProcess` function")
            runProcess = { runBlocking { method.callSuspend(dindService, it) as Result<Boolean> } }
        }
    }

    @Test
    fun `do ping localhost`() {
        Assertions.assertDoesNotThrow {
            Assertions.assertTrue(runProcess(arrayOf("ping", "-n", "1", "-w", "-5", "localhost")).getOrThrow())
        }
    }

    @Test
    fun `do sleep 999 seconds`() {
        try {
            Assertions.assertTimeout(5.toDuration(DurationUnit.SECONDS).toJavaDuration()) {
                runProcess(arrayOf("sleep", "99999")).getOrThrow()
            }
        } catch (e: IllegalStateException) {
            Assertions.assertEquals("Waiting time elapsed", e.message)
        }
    }

    @Test
    fun `do exit 1`() {
        try {
            Assertions.assertTimeout(5.toDuration(DurationUnit.SECONDS).toJavaDuration()) {
                runProcess(arrayOf("exit", "1")).getOrThrow()
            }
        } catch (e: IllegalStateException) {
            Assertions.assertNotNull(e.message)
            Assertions.assertTrue("failed with" in e.message!!)
        }
    }
}
