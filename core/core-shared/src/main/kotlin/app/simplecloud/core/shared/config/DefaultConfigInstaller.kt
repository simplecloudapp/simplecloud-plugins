package app.simplecloud.core.shared.config

import java.nio.file.Files
import java.nio.file.Path

object DefaultConfigInstaller {

    fun install(target: Path, classLoader: ClassLoader) {
        copy(target, target.fileName.toString(), classLoader)
    }

    fun installAll(
        dataDirectory: Path,
        resources: List<String>,
        classLoader: ClassLoader,
        resourcePrefix: String = "",
    ) {
        val prefix = if (resourcePrefix.isEmpty() || resourcePrefix.endsWith("/")) resourcePrefix else "$resourcePrefix/"
        resources.forEach { resource ->
            copy(dataDirectory.resolve(resource), prefix + resource, classLoader)
        }
    }

    private fun copy(target: Path, resourceName: String, classLoader: ClassLoader) {
        if (Files.exists(target)) return

        val resource = checkNotNull(classLoader.getResourceAsStream(resourceName)) { "Missing bundled $resourceName" }
        Files.createDirectories(target.parent)
        resource.use { stream -> Files.copy(stream, target) }
    }
}
