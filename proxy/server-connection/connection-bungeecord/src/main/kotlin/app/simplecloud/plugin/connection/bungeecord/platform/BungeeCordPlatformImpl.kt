package app.simplecloud.plugin.connection.bungeecord.platform

import app.simplecloud.plugin.connection.bungeecord.registration.BungeeCordServerRegistry
import app.simplecloud.plugin.connection.shared.platform.ConnectionPlatform
import net.md_5.bungee.api.plugin.Plugin
import java.io.File

class BungeeCordPlatformImpl(
    private val plugin: Plugin,
) : ConnectionPlatform<BungeeCordConnectionServer> {

    private val registry = BungeeCordServerRegistry(plugin.proxy)

    override fun getDataDirectory(): File = plugin.dataFolder

    override fun getRegistry(): BungeeCordServerRegistry = registry

    override fun getServers(): List<BungeeCordConnectionServer> = plugin.proxy.servers.values.map { serverInfo ->
        BungeeCordConnectionServer(serverInfo)
    }
}
