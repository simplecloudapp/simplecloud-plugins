package app.simplecloud.core.shared.config.migration

import org.spongepowered.configurate.NodePath
import org.spongepowered.configurate.transformation.TransformAction

data class ConfigMigration(
    val version: Int,
    val path: NodePath,
    val action: TransformAction,
)
