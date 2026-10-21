package app.simplecloud.plugin.connection.bungeecord.platform

import app.simplecloud.plugin.connection.shared.platform.ConnectionPlayer
import net.md_5.bungee.api.connection.ProxiedPlayer

class BungeeCordConnectionPlayer(
    private val player: ProxiedPlayer,
) : ConnectionPlayer {

    override fun hasPermission(permission: String): Boolean = player.hasPermission(permission)

    override fun getVirtualHost(): String? = player.pendingConnection.virtualHost?.hostName
}
