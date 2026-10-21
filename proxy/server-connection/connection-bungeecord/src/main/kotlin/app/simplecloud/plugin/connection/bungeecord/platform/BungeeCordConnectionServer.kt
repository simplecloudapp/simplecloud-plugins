package app.simplecloud.plugin.connection.bungeecord.platform

import app.simplecloud.plugin.connection.shared.ConnectionPlugin
import app.simplecloud.plugin.connection.shared.platform.ConnectionServer
import net.md_5.bungee.api.config.ServerInfo

class BungeeCordConnectionServer(
    val serverInfo: ServerInfo,
    private val connectionPlugin: ConnectionPlugin,
) : ConnectionServer {

    override fun getName(): String = serverInfo.name

    override fun getPlayerCount(): Int = serverInfo.players.size

    override fun isStopping(): Boolean = connectionPlugin.isStopping(serverInfo.name)
}
