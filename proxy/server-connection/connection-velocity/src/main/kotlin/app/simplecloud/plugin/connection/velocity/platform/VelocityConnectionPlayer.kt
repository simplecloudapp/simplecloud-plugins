package app.simplecloud.plugin.connection.velocity.platform

import app.simplecloud.plugin.connection.shared.platform.ConnectionPlayer
import com.velocitypowered.api.proxy.Player
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

class VelocityConnectionPlayer(
    private val player: Player,
) : ConnectionPlayer {

    override fun getUniqueId(): UUID = player.uniqueId

    override fun hasPermission(permission: String): Boolean = player.hasPermission(permission)

    override fun getVirtualHost(): String? = player.virtualHost.getOrNull()?.hostName

    override fun getServerName(): String? = player.currentServer.getOrNull()?.serverInfo?.name
}
