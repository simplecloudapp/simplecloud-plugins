package app.simplecloud.plugin.core.shared.config.migration

import org.spongepowered.configurate.CommentedConfigurationNode

/** A single migration step that modifies a yaml node in place. */
fun interface ConfigMigration {
    fun migrate(node: CommentedConfigurationNode)
}
