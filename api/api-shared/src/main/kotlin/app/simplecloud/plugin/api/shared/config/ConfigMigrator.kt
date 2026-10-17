package app.simplecloud.plugin.api.shared.config

import org.spongepowered.configurate.CommentedConfigurationNode

/**
 * Applies sequential migrations to bring a yaml config node to the current version.
 */
fun interface ConfigMigration {
    fun migrate(node: CommentedConfigurationNode)
}

class ConfigMigrator private constructor(
    private val currentVersion: Int,
    private val fallbackVersion: Int,
    private val migrationsByFromVersion: Map<Int, MigrationStep>,
) {

    init {
        require(currentVersion >= fallbackVersion) {
            "The current config version must be greater than or equal to the fallback version"
        }
    }

    fun migrate(node: CommentedConfigurationNode): Boolean {
        var version = node.node(VERSION_NODE).getInt(this.fallbackVersion)

        require(version <= this.currentVersion) {
            "The config version $version is newer than the supported version ${this.currentVersion}"
        }

        var migrated = false
        while (version < this.currentVersion) {
            val migration = this.migrationsByFromVersion[version]
                ?: throw IllegalStateException(
                    "Missing config migration from version $version to ${this.currentVersion}"
                )

            migration.action.migrate(node)
            version = migration.toVersion
            node.node(VERSION_NODE).set(version)
            migrated = true
        }

        return migrated
    }

    class Builder internal constructor(
        private val currentVersion: Int,
    ) {

        private var fallbackVersion: Int = 0
        private val migrations = mutableMapOf<Int, MigrationStep>()

        fun fallbackVersion(version: Int): Builder {
            require(version >= 0) {
                "The fallback version must be greater than or equal to 0"
            }
            this.fallbackVersion = version
            return this
        }

        fun migrate(
            fromVersion: Int,
            toVersion: Int,
            action: ConfigMigration,
        ): Builder {
            require(fromVersion >= 0) {
                "The source config version must be greater than or equal to 0"
            }
            require(toVersion > fromVersion) {
                "The target config version must be greater than the source config version"
            }
            require(toVersion <= this.currentVersion) {
                "The target config version must not exceed the current config version"
            }
            require(fromVersion !in this.migrations) {
                "A migration from version $fromVersion is already registered"
            }

            this.migrations[fromVersion] = MigrationStep(toVersion, action)
            return this
        }

        fun build(): ConfigMigrator {
            return ConfigMigrator(
                currentVersion = this.currentVersion,
                fallbackVersion = this.fallbackVersion,
                migrationsByFromVersion = this.migrations.toMap(),
            )
        }
    }

    private data class MigrationStep(
        val toVersion: Int,
        val action: ConfigMigration,
    )

    companion object {
        const val VERSION_NODE = "version"

        fun builder(currentVersion: Int): Builder {
            require(currentVersion >= 0) {
                "The current config version must be greater than or equal to 0"
            }
            return Builder(currentVersion)
        }
    }
}
