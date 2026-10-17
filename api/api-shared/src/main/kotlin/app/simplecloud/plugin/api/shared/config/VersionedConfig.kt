package app.simplecloud.plugin.api.shared.config

/**
 * Marks a configuration as versioned so it can participate in yaml migrations.
 */
interface VersionedConfig {
    val version: Int
}
