package app.simplecloud.plugin.proxy.shared.config.migration

import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

object LegacyDirectoryMigrator {

    private val logger = LoggerFactory.getLogger(LegacyDirectoryMigrator::class.java)
    private val LEGACY_DIRECTORY_NAMES = listOf("proxy-essentials-velocity", "proxy-essentials-bungeecord")

    fun migrate(dataDirectory: Path) {
        val directory = dataDirectory.toAbsolutePath().normalize()
        val pluginsDirectory = directory.parent ?: return
        val legacyDirectory = LEGACY_DIRECTORY_NAMES
            .map { pluginsDirectory.resolve(it) }
            .find { it != directory && Files.isDirectory(it) } ?: return

        if (Files.isDirectory(directory) && Files.list(directory).use { it.findAny().isPresent }) {
            logger.warn(
                "The directory '${legacyDirectory.fileName}' is not used anymore, so changes to it are ignored. " +
                    "Move your configuration to '${directory.fileName}' and remove '${legacyDirectory.fileName}' " +
                    "from your template.",
            )
            return
        }

        try {
            moveOrCopy(legacyDirectory, directory)
        } catch (e: Exception) {
            logger.error(
                "Could not take over the configuration of '${legacyDirectory.fileName}', " +
                    "so the default configuration is used",
                e,
            )
            return
        }

        logger.info(
            "Moved the configuration of '${legacyDirectory.fileName}' to '${directory.fileName}'. " +
                "Rename that directory in your template as well.",
        )
    }

    private fun moveOrCopy(source: Path, target: Path) {
        Files.deleteIfExists(target)
        try {
            Files.move(source, target)
        } catch (e: Exception) {
            logger.warn("Could not move '${source.fileName}', copying it instead", e)
            source.toFile().copyRecursively(target.toFile(), overwrite = true)
            if (!source.toFile().deleteRecursively()) {
                logger.warn("Could not delete '${source.fileName}', changes to it are ignored")
            }
        }
    }
}
