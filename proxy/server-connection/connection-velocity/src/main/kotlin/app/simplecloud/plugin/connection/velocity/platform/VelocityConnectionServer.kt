package app.simplecloud.plugin.connection.velocity.platform

import app.simplecloud.plugin.connection.shared.platform.ConnectionServer
import com.velocitypowered.api.proxy.server.RegisteredServer

class VelocityConnectionServer(
    val server: RegisteredServer,
) : ConnectionServer {

    override fun getName(): String = server.serverInfo.name

    override fun getPlayerCount(): Int = server.playersConnected.size
}
