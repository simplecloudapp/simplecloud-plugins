package app.simplecloud.plugin.connection.waterdog.platform

import app.simplecloud.plugin.connection.shared.platform.ConnectionPlayer
import dev.waterdog.waterdogpe.player.ProxiedPlayer

class WaterdogConnectionPlayer(
    private val player: ProxiedPlayer,
) : ConnectionPlayer {

    override fun hasPermission(permission: String): Boolean = player.hasPermission(permission)

    override fun getVirtualHost(): String? = player.loginData.joinHostname
}
