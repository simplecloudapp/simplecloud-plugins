package app.simplecloud.plugin.connection.shared.connection

import app.simplecloud.plugin.connection.shared.platform.ConnectionServer

class FakeServer(
    val serverName: String,
    private val playerCount: Int,
) : ConnectionServer {

    override fun getName(): String = serverName

    override fun getPlayerCount(): Int = playerCount
}
