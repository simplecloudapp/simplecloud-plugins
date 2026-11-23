package app.simplecloud.plugin.core.shared.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.seconds

class ConfigWatcher(
    private val directory: Path,
    private val onChange: () -> Unit,
) {

    private val logger = LoggerFactory.getLogger(ConfigWatcher::class.java)
    private val loaded = AtomicReference<Map<Path, FileState>>(emptyMap())
    private val lastCheck = AtomicReference<Map<Path, FileState>>(emptyMap())

    fun start(scope: CoroutineScope): Job {
        val initial = snapshot().orEmpty()
        loaded.set(initial)
        lastCheck.set(initial)

        return scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(2.seconds)
                poll()
            }
        }
    }

    private fun poll() {
        val current = snapshot() ?: return
        val previous = lastCheck.getAndSet(current)
        if (current != previous || current == loaded.get()) return

        reload(findChangedFiles(loaded.get(), current))
        val reloaded = snapshot() ?: current
        loaded.set(reloaded)
        lastCheck.set(reloaded)
    }

    private fun reload(changedFiles: List<Path>) {
        logger.info("Reloading the configs because {} changed", changedFiles.joinToString { file -> file.toString() })
        try {
            onChange()
        } catch (e: Exception) {
            logger.error("Failed to reload the configs in {}", directory, e)
        }
    }

    private fun findChangedFiles(previous: Map<Path, FileState>, current: Map<Path, FileState>): List<Path> =
        (previous.keys + current.keys)
            .filter { file -> previous[file] != current[file] }
            .map { file -> directory.relativize(file) }
            .sorted()

    private fun snapshot(): Map<Path, FileState>? {
        if (Files.notExists(directory)) return emptyMap()

        return try {
            Files.walk(directory).use { paths ->
                paths
                    .filter { path -> Files.isRegularFile(path) && isYaml(path) }
                    .toList()
                    .associateWith { path -> FileState(Files.getLastModifiedTime(path), Files.size(path)) }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun isYaml(path: Path): Boolean {
        val name = path.fileName.toString()
        return name.endsWith(".yml") || name.endsWith(".yaml")
    }

    private data class FileState(
        val modified: FileTime,
        val size: Long,
    )
}
