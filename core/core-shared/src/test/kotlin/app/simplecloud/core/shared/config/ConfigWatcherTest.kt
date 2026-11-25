package app.simplecloud.core.shared.config

import app.simplecloud.core.shared.config.command.CommandExceptionMessages
import kotlinx.coroutines.*
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.*
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ConfigWatcherTest {

    @Test
    fun `reloads once after a yaml file changed`() = runBlocking {
        val directory = Files.createTempDirectory("config-watcher")
        val config = directory.resolve("config.yml")
        config.writeText("a: 1\n")
        val reloads = AtomicInteger()
        val scope = CoroutineScope(Dispatchers.IO)
        ConfigWatcher(directory) { reloads.incrementAndGet() }.start(scope)

        config.writeText("a: 12\n")
        awaitReloads(reloads, 1)
        delay(6.seconds)

        assertEquals(1, reloads.get())
        scope.cancel()
    }

    @Test
    fun `ignores files that are not yaml`() = runBlocking {
        val directory = Files.createTempDirectory("config-watcher")
        val reloads = AtomicInteger()
        val scope = CoroutineScope(Dispatchers.IO)
        ConfigWatcher(directory) { reloads.incrementAndGet() }.start(scope)

        directory.resolve("server-icon.png").writeText("png")
        delay(6.seconds)

        assertEquals(0, reloads.get())
        scope.cancel()
    }

    @Test
    fun `reloads when a yaml file is added to a subdirectory`() = runBlocking {
        val directory = Files.createTempDirectory("config-watcher")
        val reloads = AtomicInteger()
        val scope = CoroutineScope(Dispatchers.IO)
        ConfigWatcher(directory) { reloads.incrementAndGet() }.start(scope)

        directory.resolve("layouts").createDirectories().resolve("lobby.yml").writeText("a: 1\n")
        awaitReloads(reloads, 1)

        assertEquals(1, reloads.get())
        scope.cancel()
    }

    @Test
    fun `keeps the cached config when the file has yaml errors`() {
        val file = Files.createTempFile("config-watcher", ".yml").toFile()
        file.deleteOnExit()
        file.writeText("syntax: custom\n")
        val factory = ConfigurationFactory(file, CommandExceptionMessages::class.java)
        factory.reload()

        file.writeText("syntax: [custom\n")

        assertFails { factory.reload() }
        assertEquals("custom", factory.get().syntax)
    }

    private suspend fun awaitReloads(reloads: AtomicInteger, expected: Int) {
        withTimeout(10.seconds) {
            while (reloads.get() < expected) delay(50.milliseconds)
        }
    }
}
