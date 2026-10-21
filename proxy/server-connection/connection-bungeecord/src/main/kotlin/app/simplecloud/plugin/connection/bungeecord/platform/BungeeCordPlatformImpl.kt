package app.simplecloud.plugin.connection.bungeecord.platform

import app.simplecloud.plugin.connection.shared.platform.ConnectionPlatform
import net.md_5.bungee.api.ProxyServer

class BungeeCordPlatformImpl(
    private val proxy: ProxyServer,
) : ConnectionPlatform<BungeeCordConnectionServer> {

    override fun getServers(): List<BungeeCordConnectionServer> = proxy.servers.values.map(::BungeeCordConnectionServer)
}
