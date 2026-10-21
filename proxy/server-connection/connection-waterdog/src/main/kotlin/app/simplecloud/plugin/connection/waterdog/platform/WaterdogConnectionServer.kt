package app.simplecloud.plugin.connection.waterdog.platform

import app.simplecloud.plugin.connection.shared.platform.ConnectionServer
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo

class WaterdogConnectionServer(
    val serverInfo: ServerInfo,
) : ConnectionServer {

    override fun getName(): String = serverInfo.serverName

    override fun getPlayerCount(): Int = serverInfo.players.size
}
