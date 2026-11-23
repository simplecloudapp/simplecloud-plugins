package app.simplecloud.plugin.connection.velocity.platform

import app.simplecloud.plugin.connection.shared.platform.ConnectionPlatform
import app.simplecloud.plugin.connection.shared.platform.ConnectionServer
import app.simplecloud.plugin.connection.velocity.VelocityConnectionPlugin
import app.simplecloud.plugin.connection.velocity.command.VelocityCommandManager
import app.simplecloud.plugin.connection.velocity.registration.VelocityServerRegistry
import com.velocitypowered.api.proxy.ProxyServer
import java.io.File
import java.nio.file.Path

class VelocityPlatformImpl(
    plugin: VelocityConnectionPlugin,
    private val proxy: ProxyServer,
    private val dataDirectory: Path,
) : ConnectionPlatform {

    private val registry = VelocityServerRegistry(proxy)
    private val commandManager = VelocityCommandManager(plugin, proxy)

    override fun getDataDirectory(): File = dataDirectory.toFile()

    override fun getRegistry(): VelocityServerRegistry = registry

    override fun getCommandManager(): VelocityCommandManager = commandManager

    override fun getServers(): List<ConnectionServer> = proxy.allServers.map { server ->
        ConnectionServer(server.serverInfo.name, server.playersConnected.size)
    }
}
