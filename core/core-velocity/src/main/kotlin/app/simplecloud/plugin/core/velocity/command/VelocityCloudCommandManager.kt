package app.simplecloud.plugin.core.velocity.command

import com.velocitypowered.api.proxy.ProxyServer
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.velocity.VelocityCommandManager

/** Creates command managers for Velocity plugins. */
object VelocityCloudCommandManager {

    /** Creates a [VelocityCommandManager] using [VelocityCloudSender]s for the given [plugin] instance. */
    fun create(
        plugin: Any,
        server: ProxyServer,
        coordinator: ExecutionCoordinator<VelocityCloudSender> = ExecutionCoordinator.asyncCoordinator(),
    ): VelocityCommandManager<VelocityCloudSender> = VelocityCommandManager(
        server.pluginManager.ensurePluginContainer(plugin),
        server,
        coordinator,
        VelocityCloudSenderMapper,
    )
}
