package app.simplecloud.plugin.connection.bungeecord.listener

import app.simplecloud.plugin.connection.bungeecord.BungeeCordConnectionPlugin
import app.simplecloud.plugin.connection.bungeecord.platform.BungeeCordConnectionPlayer
import app.simplecloud.plugin.connection.bungeecord.platform.BungeeCordPlatformImpl
import app.simplecloud.plugin.connection.shared.connection.ConnectionResolver
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class ServerConnectListener(
    private val plugin: BungeeCordConnectionPlugin,
    private val platform: BungeeCordPlatformImpl,
) : Listener {

    @EventHandler
    fun onServerConnect(event: ServerConnectEvent) {
        when (event.reason) {
            ServerConnectEvent.Reason.JOIN_PROXY -> handleJoin(event)
            ServerConnectEvent.Reason.LOBBY_FALLBACK -> handleLobbyFallback(event)
            ServerConnectEvent.Reason.SERVER_DOWN_REDIRECT -> handleServerDown(event)
            else -> return
        }
    }

    private fun handleJoin(event: ServerConnectEvent) {
        val config = plugin.connectionPlugin.connectionConfig.get()
        val messages = plugin.connectionPlugin.messageConfig.get()

        val targetServer = ConnectionResolver.findJoinTarget(platform, BungeeCordConnectionPlayer(event.player), config)
        if (targetServer != null) {
            event.target = targetServer.serverInfo
            return
        }

        if (!config.networkJoinTargets.enabled) return

        event.isCancelled = true
        event.player.disconnect(
            *BungeeComponentSerializer.get().serialize(messages.msg(messages.kick.noTargetConnection)),
        )
    }

    private fun handleLobbyFallback(event: ServerConnectEvent) {
        if (event.player.server != null) {
            event.isCancelled = true
            return
        }

        val config = plugin.connectionPlugin.connectionConfig.get()
        val targetServer = ConnectionResolver.findJoinTarget(platform, BungeeCordConnectionPlayer(event.player), config) ?: return
        event.target = targetServer.serverInfo
    }

    private fun handleServerDown(event: ServerConnectEvent) {
        val config = plugin.connectionPlugin.connectionConfig.get()
        val messages = plugin.connectionPlugin.messageConfig.get()

        if (!config.fallback.enabled) return

        val downServerName = event.player.server?.info?.name ?: return
        val targetServer = ConnectionResolver.findFallbackTarget(
            platform,
            BungeeCordConnectionPlayer(event.player),
            downServerName,
            config,
        )
        if (targetServer != null) {
            event.target = targetServer.serverInfo
            return
        }

        event.isCancelled = true
        event.player.disconnect(
            *BungeeComponentSerializer.get().serialize(messages.msg(messages.kick.noFallbackServers)),
        )
    }
}
