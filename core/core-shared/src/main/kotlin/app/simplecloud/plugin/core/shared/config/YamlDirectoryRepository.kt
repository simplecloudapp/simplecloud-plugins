package app.simplecloud.plugin.core.shared.config

import app.simplecloud.plugin.core.shared.config.migration.ConfigMigrations
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

abstract class YamlDirectoryRepository<E : Any, I>(
    private val directory: Path,
    javaClass: Class<E>,
    migrations: ConfigMigrations? = null,
) {

    private val configurator = YamlFileConfigurator(javaClass, migrations)

    private val cachedEntities = ConcurrentHashMap<File, E>()

    abstract fun save(entity: E)

    abstract fun find(identifier: I): E?

    fun findAll(): List<E> = cachedEntities.values.toList()

    fun load(): List<E> {
        Files.createDirectories(directory)
        cachedEntities.clear()

        return Files.walk(directory).use { paths ->
            paths
                .toList()
                .map { path -> path.toFile() }
                .filter { file -> file.extension == "yml" || file.extension == "yaml" }
                .mapNotNull { file -> load(file) }
        }
    }

    fun load(file: File): E? {
        val entity = configurator.load(file) ?: return null
        cachedEntities[file] = entity
        return entity
    }

    protected fun save(name: String, entity: E) {
        val file = directory.resolve(name).toFile()
        configurator.save(file, entity)
        cachedEntities[file] = entity
    }

    fun delete(entity: E) {
        val file = cachedEntities.entries.find { entry -> entry.value === entity }?.key ?: return
        file.delete()
        cachedEntities.remove(file)
    }
}
