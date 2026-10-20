package app.simplecloud.plugin.core.bungeecord.command

import net.md_5.bungee.api.plugin.Plugin
import org.incendo.cloud.bungee.BungeeCommandManager
import org.incendo.cloud.execution.ExecutionCoordinator

/** Creates command managers for BungeeCord plugins. */
object BungeeCloudCommandManager {

    /** Creates a [BungeeCommandManager] using [BungeeCloudSender]s for the given [plugin]. */
    fun create(
        plugin: Plugin,
        coordinator: ExecutionCoordinator<BungeeCloudSender> = ExecutionCoordinator.asyncCoordinator(),
    ): BungeeCommandManager<BungeeCloudSender> = BungeeCommandManager(
        plugin,
        coordinator,
        BungeeCloudSenderMapper(),
    )
}
