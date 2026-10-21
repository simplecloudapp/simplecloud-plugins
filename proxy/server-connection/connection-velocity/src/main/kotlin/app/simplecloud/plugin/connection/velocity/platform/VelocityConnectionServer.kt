package app.simplecloud.plugin.connection.velocity.platform

import app.simplecloud.plugin.connection.shared.ConnectionPlugin
import app.simplecloud.plugin.connection.shared.platform.ConnectionServer
import com.velocitypowered.api.proxy.server.RegisteredServer

class VelocityConnectionServer(
    val server: RegisteredServer,
    private val connectionPlugin: ConnectionPlugin,
) : ConnectionServer {

    override fun getName(): String = server.serverInfo.name

    override fun getPlayerCount(): Int = server.playersConnected.size

    override fun isStopping(): Boolean = connectionPlugin.isStopping(server.serverInfo.name)
}
