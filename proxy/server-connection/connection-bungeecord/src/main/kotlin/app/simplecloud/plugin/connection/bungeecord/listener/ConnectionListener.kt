package app.simplecloud.plugin.connection.bungeecord.listener

import app.simplecloud.plugin.connection.bungeecord.platform.BungeeCordConnectionPlayer
import app.simplecloud.plugin.connection.shared.ConnectionPlugin
import app.simplecloud.plugin.connection.shared.connection.KickTracker
import app.simplecloud.plugin.core.bungeecord.text.BungeeComponentConverter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.event.ServerKickEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ConnectionListener(
    private val connectionPlugin: ConnectionPlugin,
    private val proxy: ProxyServer,
) : Listener {

    private val attemptedServers = ConcurrentHashMap<UUID, Set<String>>()
    private val kickTracker = KickTracker()

    @EventHandler
    fun onServerConnect(event: ServerConnectEvent) {
        when (event.reason) {
            ServerConnectEvent.Reason.JOIN_PROXY -> handleJoin(event)
            ServerConnectEvent.Reason.LOBBY_FALLBACK -> handleLobbyFallback(event)
            ServerConnectEvent.Reason.SERVER_DOWN_REDIRECT -> handleServerDown(event)
            else -> return
        }
    }

    @EventHandler
    fun onServerConnected(event: ServerConnectedEvent) {
        attemptedServers.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        attemptedServers.remove(event.player.uniqueId)
        kickTracker.remove(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onServerKick(event: ServerKickEvent) {
        val config = connectionPlugin.connectionConfig.get()
        val messages = connectionPlugin.messageConfig.get()

        if (!config.fallback.enabled) return

        val player = event.player
        val refusedWhileConnecting = event.state == ServerKickEvent.State.CONNECTING
        val fallingBack = attemptedServers.containsKey(player.uniqueId)
        if (refusedWhileConnecting && player.server != null && !fallingBack) return

        val kickedServerNames = kickTracker.add(player.uniqueId, event.kickedFrom.name)
        val excludedServerNames = attemptedServers[player.uniqueId].orEmpty() + kickedServerNames
        val targetServerName = connectionPlugin.resolver.findFallbackTarget(
            config,
            BungeeCordConnectionPlayer(player, proxy),
            event.kickedFrom.name,
            excludedServerNames,
        )
        val targetServer = findServer(targetServerName)
        if (targetServer != null) {
            attemptedServers[player.uniqueId] = excludedServerNames + targetServer.name
            event.cancelServer = targetServer
            event.isCancelled = true
            return
        }

        if (refusedWhileConnecting) return

        val kickReason = event.reason
        val reason = if (kickReason == null) Component.empty() else BungeeComponentConverter.fromBungee(kickReason)
        val message = messages.msg(
            messages.kick.noFallbackServers,
            Placeholder.component("reason", reason),
            Placeholder.unparsed("server", event.kickedFrom.name),
        )
        event.reason = BungeeComponentConverter.toBungee(message)
    }

    private fun handleJoin(event: ServerConnectEvent) {
        val config = connectionPlugin.connectionConfig.get()
        val messages = connectionPlugin.messageConfig.get()

        val targetServerName = connectionPlugin.resolver.findJoinTarget(
            config,
            BungeeCordConnectionPlayer(event.player, proxy),
        )
        val targetServer = findServer(targetServerName)
        if (targetServer != null) {
            attemptedServers[event.player.uniqueId] = setOf(targetServer.name)
            event.target = targetServer
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

        val config = connectionPlugin.connectionConfig.get()
        val messages = connectionPlugin.messageConfig.get()
        val excludedServerNames = attemptedServers[event.player.uniqueId].orEmpty()
        val targetServerName = connectionPlugin.resolver.findJoinTarget(
            config,
            BungeeCordConnectionPlayer(event.player, proxy),
            excludedServerNames,
        )
        val targetServer = findServer(targetServerName)
        if (targetServer != null) {
            attemptedServers[event.player.uniqueId] = excludedServerNames + targetServer.name
            event.target = targetServer
            return
        }

        if (!config.networkJoinTargets.enabled) return

        event.isCancelled = true
        event.player.disconnect(BungeeComponentConverter.toBungee(messages.msg(messages.kick.noTargetConnection)))
    }

    private fun handleServerDown(event: ServerConnectEvent) {
        val config = connectionPlugin.connectionConfig.get()
        val messages = connectionPlugin.messageConfig.get()

        if (!config.fallback.enabled) return

        val downServerName = event.player.server?.info?.name ?: return
        val kickedServerNames = kickTracker.add(event.player.uniqueId, downServerName)
        val targetServerName = connectionPlugin.resolver.findFallbackTarget(
            config,
            BungeeCordConnectionPlayer(event.player, proxy),
            downServerName,
            kickedServerNames,
        )
        val targetServer = findServer(targetServerName)
        if (targetServer != null) {
            attemptedServers[event.player.uniqueId] = kickedServerNames + targetServer.name
            event.target = targetServer
            return
        }

        event.isCancelled = true
        val message = messages.msg(
            messages.kick.noFallbackServers,
            Placeholder.component("reason", Component.empty()),
            Placeholder.unparsed("server", downServerName),
        )
        event.player.disconnect(BungeeComponentConverter.toBungee(message))
    }

    private fun findServer(serverName: String?): ServerInfo? {
        if (serverName == null) return null
        return proxy.getServerInfo(serverName)
    }
}
