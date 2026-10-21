package app.simplecloud.plugin.connection.waterdog.platform

import app.simplecloud.plugin.connection.shared.ConnectionPlugin
import app.simplecloud.plugin.connection.shared.platform.ConnectionPlatform
import dev.waterdog.waterdogpe.ProxyServer

class WaterdogPlatformImpl(
    private val proxy: ProxyServer,
    private val connectionPlugin: ConnectionPlugin,
) : ConnectionPlatform<WaterdogConnectionServer> {

    override fun getServers(): List<WaterdogConnectionServer> = proxy.servers.map { serverInfo ->
        WaterdogConnectionServer(serverInfo, connectionPlugin)
    }
}
