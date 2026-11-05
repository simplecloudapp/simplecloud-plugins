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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private typealias FileStates = Map<Path, Pair<FileTime, Long>>

// polls instead of using a WatchService, because each WatchService uses up an inotify instance
// and many servers on one host would run out of them
class ConfigWatcher(
    private val directory: Path,
    private val interval: Duration = DEFAULT_INTERVAL,
    private val onChange: () -> Unit,
) {

    private val logger = LoggerFactory.getLogger(ConfigWatcher::class.java)

    fun start(scope: CoroutineScope): Job {
        val initial = snapshot().orEmpty()
        return scope.launch(Dispatchers.IO) {
            var loaded = initial
            var lastCheck = initial

            while (isActive) {
                delay(interval)
                val current = snapshot() ?: continue

                val stillChanging = current != lastCheck
                lastCheck = current
                if (stillChanging || current == loaded) continue

                reload()
                // the reload can write files itself, e.g. a migrated config, which must not trigger another reload
                loaded = snapshot() ?: current
                lastCheck = loaded
            }
        }
    }

    private fun reload() {
        logger.info("Reloading the configs because a file in {} changed", directory)
        try {
            onChange()
        } catch (e: Exception) {
            logger.error("Failed to reload the configs in {}", directory, e)
        }
    }

    private fun snapshot(): FileStates? {
        if (Files.notExists(directory)) return emptyMap()

        return try {
            Files.walk(directory).use { paths ->
                paths
                    .filter { Files.isRegularFile(it) && isYaml(it) }
                    .toList()
                    .associateWith { Files.getLastModifiedTime(it) to Files.size(it) }
            }
        } catch (_: Exception) {
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
