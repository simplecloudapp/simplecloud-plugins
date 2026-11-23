package app.simplecloud.plugin.core.shared.config

import app.simplecloud.plugin.core.shared.config.migration.ConfigMigrations
import app.simplecloud.plugin.core.shared.config.serializer.GenericEnumSerializer
import org.slf4j.LoggerFactory
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentHashMap

class YamlFileConfigurator<E>(
    private val javaClass: Class<E>,
    private val migrations: ConfigMigrations? = null,
) {

    init {
        require(migrations == null || VersionedConfig::class.java.isAssignableFrom(javaClass)) {
            "Config migrations can only be used with VersionedConfig types"
        }
    }

    private val logger = LoggerFactory.getLogger(YamlFileConfigurator::class.java)
    private val loaders = ConcurrentHashMap<File, YamlConfigurationLoader>()

    private val loaderBuilder = YamlConfigurationLoader.builder().nodeStyle(NodeStyle.BLOCK).defaultOptions { options ->
        options.serializers { builder ->
            builder.registerAnnotatedObjects(objectMapperFactory())
            builder.register(Enum::class.java, GenericEnumSerializer)
            builder.register({ type -> type is Class<*> && type.isEnum }, GenericEnumSerializer)
        }
    }

    fun save(file: File, entity: E) {
        val loader = loaderOf(file)
        val node = loader.load()
        node.set(javaClass, entity)
        loader.save(node)
    }

    fun load(file: File): E? {
        val loader = loaderOf(file)
        val node = loader.load()
        if (migrations != null && migrations.isOutdated(node)) {
            migrate(file, loader, node, migrations)
        }
        return node.get(javaClass)
    }

    private fun migrate(
        file: File,
        loader: YamlConfigurationLoader,
        node: CommentedConfigurationNode,
        migrations: ConfigMigrations,
    ) {
        val previousVersion = migrations.versionOf(node)
        val label = if (previousVersion < 0) "unversioned" else "v$previousVersion"
        val backup = File(file.parentFile, "${file.name}.$label.bak")
        Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING)

        migrations.migrate(node)
        loader.save(node)
        logger.info(
            "Migrated {} from version {} to {}, the old file was saved as {}",
            file.name,
            previousVersion,
            migrations.version,
            backup.name,
        )
    }

    private fun loaderOf(file: File): YamlConfigurationLoader = loaders.getOrPut(file) {
        loaderBuilder.path(file.toPath()).build()
    }
}
