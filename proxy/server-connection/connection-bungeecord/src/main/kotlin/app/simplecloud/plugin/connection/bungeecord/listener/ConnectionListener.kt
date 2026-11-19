package app.simplecloud.plugin.connection.bungeecord.listener

import app.simplecloud.plugin.connection.bungeecord.BungeeCordConnectionPlugin
import app.simplecloud.plugin.connection.bungeecord.platform.BungeeCordConnectionPlayer
import app.simplecloud.plugin.core.bungeecord.text.BungeeComponentConverter
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.event.ServerKickEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class ConnectionListener(
    private val plugin: BungeeCordConnectionPlugin,
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

    @EventHandler(priority = EventPriority.HIGH)
    fun onServerKick(event: ServerKickEvent) {
        val config = plugin.connectionPlugin.connectionConfig.get()
        val messageConfig = plugin.connectionPlugin.messageConfig.get()

        if (!config.fallback.enabled) return

        val player = event.player
        val refusedWhileConnecting = event.state == ServerKickEvent.State.CONNECTING
        val fallingBack = plugin.connectionPlugin.hasKicks(player.uniqueId)
        if (refusedWhileConnecting && player.server != null && !fallingBack) return

        val targetServer = plugin.connectionPlugin.recordKickAndFindFallbackTarget(
            BungeeCordConnectionPlayer(player),
            event.kickedFrom.name,
        )
        if (targetServer != null) {
            event.cancelServer = targetServer.serverInfo
            event.isCancelled = true
            return
        }

        if (refusedWhileConnecting) return
        event.reason = BungeeComponentConverter.toBungee(messageConfig.msg(messageConfig.kick.noFallbackServers))
    }

    @EventHandler
    fun onServerConnected(event: ServerConnectedEvent) {
        plugin.connectionPlugin.clearKicks(event.player.uniqueId)
    }

    @EventHandler
    fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        plugin.connectionPlugin.clearKicks(event.player.uniqueId)
    }

    private fun handleJoin(event: ServerConnectEvent) {
        val config = plugin.connectionPlugin.connectionConfig.get()
        val messages = plugin.connectionPlugin.messageConfig.get()

        val targetServer = plugin.connectionPlugin.findJoinTarget(BungeeCordConnectionPlayer(event.player))
        if (targetServer != null) {
            event.target = targetServer.serverInfo
            return
        }

        if (!config.networkJoinTargets.enabled) return

        event.isCancelled = true
        event.player.disconnect(BungeeComponentConverter.toBungee(messages.msg(messages.kick.noTargetConnection)))
    }

    private fun handleLobbyFallback(event: ServerConnectEvent) {
        if (event.player.server != null) {
            event.isCancelled = true
            return
        }

        val targetServer = plugin.connectionPlugin.findJoinTarget(BungeeCordConnectionPlayer(event.player)) ?: return
        event.target = targetServer.serverInfo
    }

    private fun handleServerDown(event: ServerConnectEvent) {
        val config = plugin.connectionPlugin.connectionConfig.get()
        val messages = plugin.connectionPlugin.messageConfig.get()

        if (!config.fallback.enabled) return

        val downServerName = event.player.server?.info?.name ?: return
        val targetServer = plugin.connectionPlugin.recordKickAndFindFallbackTarget(
            BungeeCordConnectionPlayer(event.player),
            downServerName,
        )
        if (targetServer != null) {
            event.target = targetServer.serverInfo
            return
        }

        event.isCancelled = true
        event.player.disconnect(BungeeComponentConverter.toBungee(messages.msg(messages.kick.noFallbackServers)))
    }
}
