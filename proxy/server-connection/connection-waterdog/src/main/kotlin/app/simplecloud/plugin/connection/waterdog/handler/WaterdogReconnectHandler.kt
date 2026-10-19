package app.simplecloud.plugin.connection.waterdog.handler

import app.simplecloud.plugin.connection.shared.connection.ConnectionResolver
import app.simplecloud.plugin.connection.waterdog.WaterdogConnectionPlugin
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

        val config = plugin.connectionPlugin.connectionConfig.get()
        if (!config.fallback.enabled) return defaultHandler.getFallbackServer(player, oldServer, reason, kickMessage)

        val kickedServerName = oldServer?.serverName
        return ConnectionResolver.matchingTargets(
            config.fallback.targetConnections,
            config.connections,
            plugin.proxy.servers.map { it.serverName },
            kickedServerName,
            player::hasPermission,
        ).firstNotNullOfOrNull { names ->
            names
                .mapNotNull { name -> plugin.proxy.getServerInfo(name) }
                .filter { it.serverName != kickedServerName }
                .minByOrNull { it.players.size }
        }
    }
}
