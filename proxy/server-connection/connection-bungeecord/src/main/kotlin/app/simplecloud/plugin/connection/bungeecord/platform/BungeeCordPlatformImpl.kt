package app.simplecloud.plugin.connection.bungeecord.platform

import app.simplecloud.plugin.connection.bungeecord.BungeeCordConnectionPlugin
import app.simplecloud.plugin.connection.bungeecord.command.BungeeCordCommandManager
import app.simplecloud.plugin.connection.bungeecord.registration.BungeeCordServerRegistry
import app.simplecloud.plugin.connection.shared.platform.ConnectionPlatform
import app.simplecloud.plugin.connection.shared.platform.ConnectionServer
import java.io.File

class BungeeCordPlatformImpl(
    private val plugin: BungeeCordConnectionPlugin,
) : ConnectionPlatform {

    private val registry = BungeeCordServerRegistry(plugin.proxy)
    private val commandManager = BungeeCordCommandManager(plugin)

    override fun getDataDirectory(): File = plugin.dataFolder

    override fun getRegistry(): BungeeCordServerRegistry = registry

    override fun getCommandManager(): BungeeCordCommandManager = commandManager

    override fun getServers(): List<ConnectionServer> = registry.findServers().map { serverInfo ->
        ConnectionServer(serverInfo.name, serverInfo.players.size)
    }
}
