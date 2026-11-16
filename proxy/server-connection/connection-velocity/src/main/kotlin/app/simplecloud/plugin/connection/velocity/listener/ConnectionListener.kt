package app.simplecloud.plugin.connection.velocity.listener

import app.simplecloud.plugin.connection.shared.ConnectionPlugin
import app.simplecloud.plugin.connection.shared.connection.KickTracker
import app.simplecloud.plugin.connection.velocity.platform.VelocityConnectionPlayer
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.KickedFromServerEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import kotlin.jvm.optionals.getOrNull

class ConnectionListener(
    private val connectionPlugin: ConnectionPlugin,
    private val proxy: ProxyServer,
) {

    private val kickTracker = KickTracker()

    @Subscribe
    fun onPlayerChooseInitialServer(event: PlayerChooseInitialServerEvent) {
        val config = connectionPlugin.connectionConfig.get()
        val messages = connectionPlugin.messageConfig.get()

        val targetServerName = connectionPlugin.resolver.findJoinTarget(VelocityConnectionPlayer(event.player, proxy))
        val targetServer = findServer(targetServerName)
        if (targetServer != null) {
            event.setInitialServer(targetServer)
            return
        }

        if (!config.networkJoinTargets.enabled) return

        event.setInitialServer(null)
        event.player.disconnect(messages.msg(messages.kick.noTargetConnection))
    }

    @Subscribe(priority = 1000)
    fun onKickedFromServer(event: KickedFromServerEvent) {
        val config = connectionPlugin.connectionConfig.get()
        val messages = connectionPlugin.messageConfig.get()

        if (!config.fallback.enabled) return
        if (event.kickedDuringServerConnect() && event.player.currentServer.getOrNull() != null) return

        val kickedServerName = event.server.serverInfo.name
        val excludedServerNames = kickTracker.add(event.player.uniqueId, kickedServerName)
        val targetServerName = connectionPlugin.resolver.findFallbackTarget(
            VelocityConnectionPlayer(event.player, proxy),
            kickedServerName,
            excludedServerNames,
        )
        val targetServer = findServer(targetServerName)

        event.result = if (targetServer != null) {
            KickedFromServerEvent.RedirectPlayer.create(targetServer)
        } else {
            val reason = Placeholder.component("reason", event.serverKickReason.orElse(Component.empty()))
            val server = Placeholder.unparsed("server", event.server.serverInfo.name)
            KickedFromServerEvent.DisconnectPlayer.create(messages.msg(messages.kick.noFallbackServers, reason, server))
        }
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        kickTracker.remove(event.player.uniqueId)
    }

    private fun findServer(serverName: String?): RegisteredServer? {
        if (serverName == null) return null
        return proxy.getServer(serverName).getOrNull()
    }
}
