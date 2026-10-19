package app.simplecloud.plugin.core.shared.config.migration

import org.spongepowered.configurate.CommentedConfigurationNode

/**
 * Applies sequential migrations to bring a yaml config node to the current version.
 *
 * ```
 * ConfigMigrator.builder(currentVersion = 2)
 *     .migrate(1, 2) { node -> ... }
 *     .build()
 * ```
 */
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

    /** Migrates the [node] to the current version and returns whether anything was migrated. */
    fun migrate(node: CommentedConfigurationNode): Boolean {
        var version = node.node(VERSION_NODE).getInt(fallbackVersion)

        require(version <= currentVersion) {
            "The config version $version is newer than the supported version $currentVersion"
        }

        var migrated = false
        while (version < currentVersion) {
            val migration =
                checkNotNull(migrationsByFromVersion[version]) {
                    "Missing config migration from version $version to $currentVersion"
                }

            migration.action.migrate(node)
            version = migration.toVersion
            node.node(VERSION_NODE).set(version)
            migrated = true
        }

        return migrated
    }

    class Builder internal constructor(private val currentVersion: Int) {

        private var fallbackVersion = 0
        private val migrations = mutableMapOf<Int, MigrationStep>()

        /** The version that is assumed if the config has no version node yet. Defaults to `0`. */
        fun fallbackVersion(version: Int): Builder {
            require(version >= 0) {
                "The fallback version must be greater than or equal to 0"
            }
            fallbackVersion = version
            return this
        }

        /** Registers a migration from [fromVersion] to [toVersion]. */
        fun migrate(fromVersion: Int, toVersion: Int, action: ConfigMigration): Builder {
            require(fromVersion >= 0) {
                "The source config version must be greater than or equal to 0"
            }
            require(toVersion > fromVersion) {
                "The target config version must be greater than the source config version"
            }
            require(toVersion <= currentVersion) {
                "The target config version must not exceed the current config version"
            }
            require(fromVersion !in migrations) {
                "A migration from version $fromVersion is already registered"
            }

            migrations[fromVersion] = MigrationStep(toVersion, action)
            return this
        }

        fun build(): ConfigMigrator =
            ConfigMigrator(
                currentVersion = currentVersion,
                fallbackVersion = fallbackVersion,
                migrationsByFromVersion = migrations.toMap(),
            )
    }

    private data class MigrationStep(val toVersion: Int, val action: ConfigMigration)

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
