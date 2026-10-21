package app.simplecloud.plugin.core.shared.config

import app.simplecloud.plugin.core.shared.config.migration.ConfigMigrator
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

abstract class YamlDirectoryRepository<E, I>(
    private val directory: Path,
    javaClass: Class<E>,
    configMigrator: ConfigMigrator? = null,
) {

    private val configurator = YamlFileConfigurator(javaClass, configMigrator)

    private val cachedEntities = mutableMapOf<File, E>()

    /** Saves the [entity] to the directory. */
    abstract fun save(entity: E)

    /** Finds a cached entity by its [identifier]. */
    abstract fun find(identifier: I): E?

    /** All cached entities. */
    fun findAll(): List<E> = cachedEntities.values.toList()

    /** Loads all `.yml` and `.yaml` files of the directory and creates it if missing. */
    fun load(): List<E> {
        Files.createDirectories(directory)
        cachedEntities.clear()

        return Files.walk(directory).use { paths ->
            paths
                .toList()
                .map { it.toFile() }
                .filter { it.extension == "yml" || it.extension == "yaml" }
                .mapNotNull { load(it) }
        }
    }

    /** Loads and caches the entity of a single [file]. */
    fun load(file: File): E? {
        val entity = configurator.load(file) ?: return null
        cachedEntities[file] = entity
        return entity
    }

    /** Saves the [entity] to the file [name] inside the directory. */
    protected fun save(name: String, entity: E) {
        val file = directory.resolve(name).toFile()
        configurator.save(file, entity)
        cachedEntities[file] = entity
    }

    /** Deletes the file of the [entity]. */
    fun delete(entity: E) {
        val file = cachedEntities.entries.find { it.value === entity }?.key ?: return
        file.delete()
        cachedEntities.remove(file)
    }
}
