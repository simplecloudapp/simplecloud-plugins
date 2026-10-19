package app.simplecloud.plugin.core.shared.config

import app.simplecloud.plugin.core.shared.config.migration.ConfigMigrator
import app.simplecloud.plugin.core.shared.config.serializer.GenericEnumSerializer
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File

class YamlFileConfigurator<E>(private val javaClass: Class<E>, private val configMigrator: ConfigMigrator? = null) {

    init {
        require(configMigrator == null || VersionedConfig::class.java.isAssignableFrom(javaClass)) {
            "Config migrations can only be used with VersionedConfig types"
        }
    }

    private val configurationLoaders = hashMapOf<File, YamlConfigurationLoader>()

    private val defaultConfigurationBuilder =
        YamlConfigurationLoader.builder().nodeStyle(NodeStyle.BLOCK).defaultOptions { options ->
            options.serializers { builder ->
                builder.registerAnnotatedObjects(objectMapperFactory())
                builder.register(Enum::class.java, GenericEnumSerializer)
                builder.register({ type -> type is Class<*> && type.isEnum }, GenericEnumSerializer)
            }
        }

    /** Writes the [entity] to the [file]. */
    fun save(file: File, entity: E) {
        val (node, loader) = buildNode(file)
        node.set(javaClass, entity)
        loader.save(node)
    }

    /** Reads the [file], migrates it if needed and saves the migrated result. */
    fun load(file: File): E? {
        val (node, loader) = buildNode(file)
        if (configMigrator?.migrate(node) == true) {
            loader.save(node)
        }
        return node.get(javaClass)
    }

    /** Loads the raw node of the [file] together with its loader. */
    fun buildNode(file: File): Pair<CommentedConfigurationNode, YamlConfigurationLoader> {
        val loader = getOrCreateConfigurationLoader(file)
        return loader.load() to loader
    }

    /** The cached loader of the [file]. */
    fun getOrCreateConfigurationLoader(file: File): YamlConfigurationLoader = configurationLoaders.getOrPut(file) {
        defaultConfigurationBuilder.path(file.toPath()).build()
    }
}
