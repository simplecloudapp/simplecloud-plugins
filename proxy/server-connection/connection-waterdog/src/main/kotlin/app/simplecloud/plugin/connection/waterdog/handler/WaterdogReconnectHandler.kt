package app.simplecloud.plugin.connection.waterdog.handler

import app.simplecloud.plugin.connection.shared.ConnectionPlugin
import app.simplecloud.plugin.connection.waterdog.WaterdogConnectionPlugin
import app.simplecloud.plugin.connection.waterdog.platform.WaterdogConnectionPlayer
import app.simplecloud.plugin.connection.waterdog.platform.WaterdogConnectionServer
import dev.waterdog.waterdogpe.network.connection.handler.IReconnectHandler
import dev.waterdog.waterdogpe.network.connection.handler.ReconnectReason
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo
import dev.waterdog.waterdogpe.player.ProxiedPlayer

class WaterdogReconnectHandler(
    private val plugin: WaterdogConnectionPlugin,
    private val defaultHandler: IReconnectHandler,
) : IReconnectHandler {

    override fun getFallbackServer(
        player: ProxiedPlayer?,
        oldServer: ServerInfo?,
        reason: ReconnectReason?,
        kickMessage: String?,
    ): ServerInfo? {
        if (player == null) return null
        val connectionPlugin = findFallbackEnabledPlugin()
            ?: return defaultHandler.getFallbackServer(player, oldServer, reason, kickMessage)

        val connectionPlayer = WaterdogConnectionPlayer(player)
        val oldServerName = oldServer?.serverName
        val targetServer = if (oldServerName == null) {
            connectionPlugin.findFallbackTarget(connectionPlayer, null)
        } else {
            connectionPlugin.recordKickAndFindFallbackTarget(connectionPlayer, oldServerName)
        }
        return targetServer?.serverInfo
    }

    override fun getTransferFailureServer(
        player: ProxiedPlayer?,
        failedServer: ServerInfo?,
        reason: ReconnectReason?,
        message: String?,
    ): ServerInfo? {
        if (player == null) return null
        val connectionPlugin = findFallbackEnabledPlugin()
            ?: return defaultHandler.getTransferFailureServer(player, failedServer, reason, message)

        val connectionPlayer = WaterdogConnectionPlayer(player)
        val targetServer = connectionPlugin.findFallbackTarget(connectionPlayer, failedServer?.serverName)
        return targetServer?.serverInfo
    }

    private fun findFallbackEnabledPlugin(): ConnectionPlugin<WaterdogConnectionServer>? {
        val connectionPlugin = plugin.connectionPlugin ?: return null
        if (!connectionPlugin.connectionConfig.get().fallback.enabled) return null
        return connectionPlugin
    }
}
