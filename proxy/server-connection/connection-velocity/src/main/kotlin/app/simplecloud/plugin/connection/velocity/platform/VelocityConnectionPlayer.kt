package app.simplecloud.plugin.connection.velocity.platform

import app.simplecloud.plugin.connection.shared.platform.ConnectionPlayer
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import org.slf4j.LoggerFactory
import kotlin.jvm.optionals.getOrNull

class VelocityConnectionPlayer(
    private val player: Player,
    private val proxy: ProxyServer,
) : ConnectionPlayer {

    private val logger = LoggerFactory.getLogger(VelocityConnectionPlayer::class.java)

    override fun hasPermission(permission: String): Boolean = player.hasPermission(permission)

    override fun getVirtualHost(): String? = player.virtualHost.getOrNull()?.hostName

    override fun getServerName(): String? = player.currentServer.getOrNull()?.serverInfo?.name

    override fun sendMessage(message: Component) {
        player.sendMessage(message)
    }

    override fun connect(serverName: String) {
        val server = proxy.getServer(serverName).getOrNull()
        if (server == null) {
            logger.warn("Could not send player ${player.username} to $serverName because it is not registered")
            return
        }
        player.createConnectionRequest(server).connectWithIndication().whenComplete { success, error ->
            if (error != null) {
                logger.warn("Failed to send player ${player.username} to $serverName", error)
                return@whenComplete
            }
            if (success != false) return@whenComplete

            logger.warn("Could not send player ${player.username} to $serverName")
        }
    }
}
