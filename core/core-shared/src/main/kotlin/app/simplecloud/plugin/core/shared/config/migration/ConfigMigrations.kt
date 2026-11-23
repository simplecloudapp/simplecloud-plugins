package app.simplecloud.plugin.core.shared.config.migration

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.NodePath
import org.spongepowered.configurate.transformation.ConfigurationTransformation
import org.spongepowered.configurate.transformation.TransformAction

abstract class ConfigMigrations(val version: Int) {

    init {
        require(version > 0) { "The config version must be greater than 0" }
    }

    protected abstract val migrations: List<ConfigMigration>

    fun versionOf(node: ConfigurationNode): Int =
        node.node(VERSION_KEY).getInt(ConfigurationTransformation.Versioned.VERSION_UNKNOWN)

    fun isOutdated(node: ConfigurationNode): Boolean {
        val nodeVersion = versionOf(node)
        require(nodeVersion <= version) {
            "The config version $nodeVersion is newer than the supported version $version"
        }
        return nodeVersion < version
    }

    fun migrate(node: ConfigurationNode) {
        if (migrations.isNotEmpty()) {
            buildTransformation().apply(node)
        }
        node.node(VERSION_KEY).set(version)
    }

    protected fun rename(version: Int, path: String, newName: String): ConfigMigration =
        action(version, path, TransformAction.rename(newName))

    protected fun move(version: Int, path: String, newPath: String): ConfigMigration {
        val target = pathOf(newPath).array()
        return action(version, path) { _, _ -> target }
    }

    protected fun remove(version: Int, path: String): ConfigMigration =
        action(version, path, TransformAction.remove())

    protected fun action(version: Int, path: String, action: TransformAction): ConfigMigration =
        ConfigMigration(version, pathOf(path), action)

    private fun buildTransformation(): ConfigurationTransformation.Versioned {
        val builder = ConfigurationTransformation.versionedBuilder().versionKey(VERSION_KEY)
        migrations.groupBy { migration -> migration.version }.toSortedMap().forEach { (migrationVersion, steps) ->
            require(migrationVersion in 1..version) {
                "The migration to version $migrationVersion is outside of 1..$version"
            }
            val transformation = ConfigurationTransformation.builder()
            steps.forEach { step -> transformation.addAction(step.path, step.action) }
            builder.addVersion(migrationVersion, transformation.build())
        }
        return builder.build()
    }

    private fun pathOf(path: String): NodePath = NodePath.of(
        path.split('.').map { key -> if (key == WILDCARD) ConfigurationTransformation.WILDCARD_OBJECT else key },
    )

    companion object {
        const val VERSION_KEY = "version"
        private const val WILDCARD = "*"
    }
}
