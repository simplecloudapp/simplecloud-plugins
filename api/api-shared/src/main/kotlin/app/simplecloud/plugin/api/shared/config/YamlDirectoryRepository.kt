package app.simplecloud.plugin.api.shared.config

import java.io.File
import java.nio.file.*

/**
 * @author Niklas Nieberler
 */
abstract class YamlDirectoryRepository<E, I> @JvmOverloads constructor(
    private val directory: Path,
    javaClass: Class<E>,
    configMigrator: ConfigMigrator? = null,
) {

    private val yamlFileConfigurator = YamlFileConfigurator(javaClass, configMigrator)
    private val fileDirectory = directory.toFile()

    private val cachedEntities = mutableMapOf<File, E>()

    /**
     * Saves an element to the file directory
     * @param entity to save
     */
    abstract fun save(entity: E)

    abstract fun find(identifier: I): E?

    /**
     * Loads all cached entities from a directory
     */
    fun findAll(): List<E> {
        return this.cachedEntities.values.toList()
    }

    /**
     * Loads the yaml directory repository with all elements
     * @return list of all file entities
     */
    fun load(): List<E> {
        if (!this.fileDirectory.exists())
            this.fileDirectory.mkdirs()

        return Files.walk(this.directory)
            .toList()
            .map { it.toFile() }
            .filter { it.name.endsWith(".yml") || it.name.endsWith(".yaml") }
            .mapNotNull { load(it) }
    }

    /**
     * Loads an entity from a file
     * @param file to load
     */
    fun load(file: File): E? {
        val entity = this.yamlFileConfigurator.load(file) ?: return null
        this.cachedEntities[file] = entity
        return entity
    }

    /**
     * Saves an entity to the yaml formation
     * @param name of the file
     * @param entity to save
     */
    protected fun save(name: String, entity: E) {
        val file = this.directory.resolve(name).toFile()
        this.yamlFileConfigurator.save(file, entity)
        this.cachedEntities[file] = entity
    }

    /**
     * Deletes an entity file
     * @param entity to delete
     */
    fun delete(entity: E) {
        val file = this.cachedEntities.keys
            .find { this.cachedEntities[it] == entity } ?: return
        delete(file)
    }

    private fun delete(file: File) {
        file.delete()
        this.cachedEntities.remove(file)
    }
}