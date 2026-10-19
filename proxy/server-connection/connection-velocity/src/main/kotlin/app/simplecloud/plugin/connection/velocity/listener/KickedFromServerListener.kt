package app.simplecloud.plugin.connection.velocity.listener

import app.simplecloud.plugin.connection.shared.connection.ConnectionResolver
import app.simplecloud.plugin.connection.velocity.VelocityConnectionPlugin
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.KickedFromServerEvent
import com.velocitypowered.api.proxy.ProxyServer
import kotlin.jvm.optionals.getOrNull

class KickedFromServerListener(
    private val plugin: VelocityConnectionPlugin,
    private val proxy: ProxyServer,
) {

    @Subscribe(priority = 1000)
    fun onKickedFromServer(event: KickedFromServerEvent) {
        val config = plugin.connectionPlugin.connectionConfig.get()
        val messages = plugin.connectionPlugin.messageConfig.get()

        if (!config.fallback.enabled) return
        if (event.kickedDuringServerConnect() && event.player.currentServer.getOrNull() != null) return

        val kickedServerName = event.server.serverInfo.name
        val server = ConnectionResolver.matchingTargets(
            config.fallback.targetConnections,
            config.connections,
            proxy.allServers.map { it.serverInfo.name },
            kickedServerName,
            event.player::hasPermission,
        ).firstNotNullOfOrNull { names ->
            names
                .mapNotNull { name -> proxy.getServer(name).getOrNull() }
                .filter { it.serverInfo.name != kickedServerName }
                .minByOrNull { it.playersConnected.size }
        }

        event.result = if (server != null) {
            KickedFromServerEvent.RedirectPlayer.create(server)
        } else {
            KickedFromServerEvent.DisconnectPlayer.create(messages.msg(messages.kick.noFallbackServers))
        }
    }
}
