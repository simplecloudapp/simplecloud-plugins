package app.simplecloud.plugin.command.velocity

import app.simplecloud.plugin.command.shared.BuildConstants
import app.simplecloud.plugin.command.shared.CommandPlugin
import app.simplecloud.plugin.command.shared.command.CloudCommandHandler
import app.simplecloud.plugin.core.velocity.command.VelocityCloudCommandManager
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import java.nio.file.Path

@Plugin(
    id = "simplecloud-command",
    name = "simplecloud-command",
    version = BuildConstants.VERSION,
    authors = ["SimpleCloud Maintainers"],
    description = "Proxy commands for managing groups, servers, and players across the network",
    url = "https://github.com/simplecloudapp/command-plugin",
    dependencies = [
        Dependency(id = "simplecloud-api"),
    ],
)
class VelocityPlugin @Inject constructor(
    private val server: ProxyServer,
    @DataDirectory val dataDirectory: Path,
) {

    private val plugin = CommandPlugin(dataDirectory.toFile())

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        CloudCommandHandler(VelocityCloudCommandManager.create(this, server), plugin).register()
        plugin.start()
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        plugin.shutdown()
    }
}
