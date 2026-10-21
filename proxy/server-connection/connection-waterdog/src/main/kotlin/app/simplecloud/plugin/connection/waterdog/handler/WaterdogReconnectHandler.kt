package app.simplecloud.plugin.connection.waterdog.handler

import app.simplecloud.plugin.connection.waterdog.WaterdogConnectionPlugin
import app.simplecloud.plugin.connection.waterdog.platform.WaterdogConnectionPlayer
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

        val connectionPlugin = plugin.connectionPlugin
        if (connectionPlugin == null || !connectionPlugin.connectionConfig.get().fallback.enabled) {
            return defaultHandler.getFallbackServer(player, oldServer, reason, kickMessage)
        }

        val targetServer = connectionPlugin.findFallbackTarget(WaterdogConnectionPlayer(player), oldServer?.serverName)
        return targetServer?.serverInfo
    }
}
