package app.simplecloud.plugin.core.paper.command

import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager

/** Creates command managers for Paper plugins. */
object PaperCloudCommandManager {

    /**
     * Creates a [PaperCommandManager] using [PaperCloudSender]s for the given [plugin]. Must be
     * called in [JavaPlugin.onEnable].
     */
    fun create(
        plugin: JavaPlugin,
        coordinator: ExecutionCoordinator<PaperCloudSender> = ExecutionCoordinator.asyncCoordinator(),
    ): PaperCommandManager<PaperCloudSender> = PaperCommandManager.builder(PaperCloudSenderMapper)
        .executionCoordinator(coordinator)
        .buildOnEnable(plugin)
}
