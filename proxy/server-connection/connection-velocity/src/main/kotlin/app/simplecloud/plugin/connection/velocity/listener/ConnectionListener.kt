package app.simplecloud.plugin.connection.velocity.listener

import app.simplecloud.plugin.connection.velocity.VelocityConnectionPlugin
import app.simplecloud.plugin.connection.velocity.platform.VelocityConnectionPlayer
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.KickedFromServerEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import kotlin.jvm.optionals.getOrNull

class ConnectionListener(
    private val plugin: VelocityConnectionPlugin,
) {

    @Subscribe
    fun onPlayerChooseInitialServer(event: PlayerChooseInitialServerEvent) {
        val config = plugin.connectionPlugin.connectionConfig.get()
        val messages = plugin.connectionPlugin.messageConfig.get()

        val targetServer = plugin.connectionPlugin.findJoinTarget(VelocityConnectionPlayer(event.player))
        if (targetServer != null) {
            event.setInitialServer(targetServer.server)
            return
        }

        if (!config.networkJoinTargets.enabled) return

        event.setInitialServer(null)
        event.player.disconnect(messages.msg(messages.kick.noTargetConnection))
    }

    @Subscribe(priority = 1000)
    fun onKickedFromServer(event: KickedFromServerEvent) {
        val config = plugin.connectionPlugin.connectionConfig.get()
        val messages = plugin.connectionPlugin.messageConfig.get()

        if (!config.fallback.enabled) return
        if (event.kickedDuringServerConnect() && event.player.currentServer.getOrNull() != null) return

        val targetServer = plugin.connectionPlugin.findFallbackTarget(
            VelocityConnectionPlayer(event.player),
            event.server.serverInfo.name,
        )

        event.result = if (targetServer != null) {
            KickedFromServerEvent.RedirectPlayer.create(targetServer.server)
        } else {
            KickedFromServerEvent.DisconnectPlayer.create(messages.msg(messages.kick.noFallbackServers))
        }
    }
}
