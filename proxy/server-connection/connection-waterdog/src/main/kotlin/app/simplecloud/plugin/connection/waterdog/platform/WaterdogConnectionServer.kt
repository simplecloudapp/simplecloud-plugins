package app.simplecloud.plugin.connection.waterdog.platform

import app.simplecloud.plugin.connection.shared.ConnectionPlugin
import app.simplecloud.plugin.connection.shared.platform.ConnectionServer
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo

class WaterdogConnectionServer(
    val serverInfo: ServerInfo,
    private val connectionPlugin: ConnectionPlugin,
) : ConnectionServer {

    override fun getName(): String = serverInfo.serverName

    override fun getPlayerCount(): Int = serverInfo.players.size

    override fun isStopping(): Boolean = connectionPlugin.isStopping(serverInfo.serverName)
}
