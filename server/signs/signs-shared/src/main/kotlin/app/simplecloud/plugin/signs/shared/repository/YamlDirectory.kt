package app.simplecloud.plugin.signs.shared.repository

import org.slf4j.LoggerFactory
import org.spongepowered.configurate.ConfigurateException
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

open class YamlDirectory<E : Any>(
    private val directory: Path,
    private val type: Class<E>,
) {

    private val logger = LoggerFactory.getLogger(YamlDirectory::class.java)
    private val entities = ConcurrentHashMap<Path, E>()

    fun findAll(): List<E> = entities.values.toList()

    fun load() {
        Files.createDirectories(directory)
        val paths = Files.walk(directory).use { walk -> walk.filter { Files.isRegularFile(it) && isYaml(it) }.toList() }
        entities.keys.retainAll(paths.toSet())
        paths.forEach { load(it) }
    }

    protected fun save(fileName: String, entity: E, previous: E?) {
        val path = entities.entries.find { it.value == previous }?.key ?: directory.resolve(fileName)
        val loader = loader(path)
        val node = loader.createNode()
        node.set(type, entity)
        loader.save(node)
        entities[path] = entity
    }

    private fun load(path: Path) {
        try {
            entities[path] = loader(path).load().get(type) ?: return
        } catch (e: ConfigurateException) {
            logger.error("Failed to load {}", path, e)
        }
    }

    private fun isYaml(path: Path): Boolean = path.fileName.toString().endsWith(".yml")

    private fun loader(path: Path): YamlConfigurationLoader = YamlConfigurationLoader.builder()
        .path(path)
        .nodeStyle(NodeStyle.BLOCK)
        .defaultOptions { options ->
            options.serializers { it.registerAnnotatedObjects(objectMapperFactory()) }
        }
        .build()
}
