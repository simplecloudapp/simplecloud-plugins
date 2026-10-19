package app.simplecloud.plugin.core.shared.config

/** Marks a config as versioned, so it can be used with a [app.simplecloud.plugin.core.shared.config.migration.ConfigMigrator]. */
interface VersionedConfig {
    val version: Int
}
