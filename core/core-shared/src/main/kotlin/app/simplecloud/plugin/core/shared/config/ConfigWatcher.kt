package app.simplecloud.plugin.core.shared.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Calls [onChange] when a `.yml` or `.yaml` file in [directory] or one of its subdirectories is created, changed or
 * deleted. The files are checked every [interval] instead of using a WatchService, because every WatchService uses
 * up one of the few inotify instances a Linux user has, and many servers on one host would run out of them.
 */
class ConfigWatcher(
    private val directory: Path,
    private val interval: Duration = DEFAULT_INTERVAL,
    private val onChange: () -> Unit,
) {

    private val logger = LoggerFactory.getLogger(ConfigWatcher::class.java)

    fun start(scope: CoroutineScope): Job {
        val initial = snapshot().orEmpty()
        return scope.launch(Dispatchers.IO) { watch(initial, initial) }
    }

    private tailrec suspend fun watch(applied: Map<Path, Pair<FileTime, Long>>, previous: Map<Path, Pair<FileTime, Long>>) {
        delay(interval)
        val current = snapshot() ?: return watch(applied, previous)

        // only reload once the files stopped changing, so a file that is still being written is not loaded
        if (current == previous && current != applied) {
            reload()
            val reloaded = snapshot() ?: current
            return watch(reloaded, reloaded)
        }

        watch(applied, current)
    }

    private fun reload() {
        logger.info("Reloading the configs because a file in {} changed", directory)
        try {
            onChange()
        } catch (e: Exception) {
            logger.error("Failed to reload the configs in {}", directory, e)
        }
    }

    private fun snapshot(): Map<Path, Pair<FileTime, Long>>? {
        if (Files.notExists(directory)) return emptyMap()

        return try {
            Files.walk(directory).use { paths ->
                paths
                    .filter { Files.isRegularFile(it) && isYaml(it) }
                    .toList()
                    .associateWith { Files.getLastModifiedTime(it) to Files.size(it) }
            }
        } catch (_: Exception) {
            // a file was changed or deleted while walking the directory, so the next check will see it
            null
        }
    }

    private fun isYaml(path: Path): Boolean {
        val name = path.fileName.toString()
        return name.endsWith(".yml") || name.endsWith(".yaml")
    }

    private companion object {
        val DEFAULT_INTERVAL = 2.seconds
    }
}
