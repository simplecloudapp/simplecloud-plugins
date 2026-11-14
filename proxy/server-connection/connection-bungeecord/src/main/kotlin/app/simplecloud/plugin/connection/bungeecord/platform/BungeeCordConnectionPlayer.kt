package app.simplecloud.plugin.connection.bungeecord.platform

import app.simplecloud.plugin.connection.shared.platform.ConnectionPlayer
import app.simplecloud.plugin.core.bungeecord.text.BungeeComponentConverter
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.ServerConnectRequest
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ServerConnectEvent
import org.slf4j.LoggerFactory

class BungeeCordConnectionPlayer(
    private val player: ProxiedPlayer,
    private val proxy: ProxyServer,
) : ConnectionPlayer {

    private val logger = LoggerFactory.getLogger(BungeeCordConnectionPlayer::class.java)

    override fun hasPermission(permission: String): Boolean = player.hasPermission(permission)

    override fun getVirtualHost(): String? = player.pendingConnection.virtualHost?.hostName

    override fun getServerName(): String? = player.server?.info?.name

    override fun sendMessage(message: Component) {
        player.sendMessage(BungeeComponentConverter.toBungee(message))
    }

    override fun connect(serverName: String) {
        val serverInfo = proxy.getServerInfo(serverName)
        if (serverInfo == null) {
            logger.warn("Could not send player ${player.name} to $serverName because it is not registered")
            return
        }
        player.connect(
            ServerConnectRequest.builder()
                .target(serverInfo)
                .reason(ServerConnectEvent.Reason.COMMAND)
                .callback { result: ServerConnectRequest.Result?, error: Throwable? ->
                    if (error != null) {
                        logger.warn("Failed to send player ${player.name} to $serverName", error)
                        return@callback
                    }
                    if (result == ServerConnectRequest.Result.SUCCESS) return@callback

                    logger.warn("Could not send player ${player.name} to $serverName ($result)")
                }
                .build(),
        )
    }
}
