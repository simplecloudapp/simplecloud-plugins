package app.simplecloud.plugin.connection.waterdog.platform

import app.simplecloud.plugin.connection.shared.platform.ConnectionPlayer
import dev.waterdog.waterdogpe.player.ProxiedPlayer
import java.util.UUID

class WaterdogConnectionPlayer(
    private val player: ProxiedPlayer,
) : ConnectionPlayer {

    override fun getUniqueId(): UUID = player.uniqueId

    override fun hasPermission(permission: String): Boolean = player.hasPermission(permission)

    override fun getVirtualHost(): String? = player.loginData.joinHostname

    override fun getServerName(): String? = player.serverInfo?.serverName
}
