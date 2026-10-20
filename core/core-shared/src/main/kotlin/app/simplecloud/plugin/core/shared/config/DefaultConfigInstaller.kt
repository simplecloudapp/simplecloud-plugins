package app.simplecloud.plugin.core.shared.config

import java.nio.file.Files
import java.nio.file.Path

/** Copies bundled default files from the jar into the data directory. */
object DefaultConfigInstaller {

    /**
     * Copies the bundled resource with the same file name as [target] if [target] does not exist
     * yet.
     */
    fun install(target: Path, classLoader: ClassLoader) {
        copy(target, target.fileName.toString(), classLoader)
    }

    /**
     * Copies all [resources] into [dataDirectory]. Paths may contain sub directories.
     */
    fun installAll(
        dataDirectory: Path,
        resources: List<String>,
        classLoader: ClassLoader,
        resourcePrefix: String = "",
    ) {
        val prefix = if (resourcePrefix.isEmpty() ||
            resourcePrefix.endsWith("/")
        ) {
            resourcePrefix
        } else {
            "$resourcePrefix/"
        }
        resources.forEach { resource ->
            copy(dataDirectory.resolve(resource), prefix + resource, classLoader)
        }
    }

    private fun copy(target: Path, resourceName: String, classLoader: ClassLoader) {
        if (Files.exists(target)) return

        val resource =
            checkNotNull(classLoader.getResourceAsStream(resourceName)) {
                "Missing bundled $resourceName"
            }

        Files.createDirectories(target.parent)
        resource.use { stream -> Files.copy(stream, target) }
    }
}
