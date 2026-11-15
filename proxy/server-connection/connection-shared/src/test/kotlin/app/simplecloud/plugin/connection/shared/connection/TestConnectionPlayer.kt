package app.simplecloud.plugin.connection.shared.connection

import app.simplecloud.plugin.connection.shared.platform.ConnectionPlayer
import net.kyori.adventure.text.Component

class TestConnectionPlayer(
    private val permissions: Set<String> = emptySet(),
    private val virtualHost: String? = null,
    private val serverName: String? = null,
) : ConnectionPlayer {

    val messages: MutableList<Component> = mutableListOf()
    val connectedServers: MutableList<String> = mutableListOf()

    override fun hasPermission(permission: String): Boolean = permission in permissions

    override fun getVirtualHost(): String? = virtualHost

    override fun getServerName(): String? = serverName

    override fun sendMessage(message: Component) {
        messages.add(message)
    }

    override fun connect(serverName: String) {
        connectedServers.add(serverName)
    }
}
