package app.simplecloud.plugin.connection.velocity.platform

import app.simplecloud.plugin.connection.shared.platform.ConnectionPlayer
import com.velocitypowered.api.proxy.Player
import kotlin.jvm.optionals.getOrNull

class VelocityConnectionPlayer(
    private val player: Player,
) : ConnectionPlayer {

    override fun hasPermission(permission: String): Boolean = player.hasPermission(permission)

    override fun getVirtualHost(): String? = player.virtualHost.getOrNull()?.hostName
}
