package app.simplecloud.plugin.connection.velocity.listener

import app.simplecloud.plugin.connection.shared.connection.ConnectionResolver
import app.simplecloud.plugin.connection.velocity.VelocityConnectionPlugin
import app.simplecloud.plugin.connection.velocity.platform.VelocityConnectionPlayer
import app.simplecloud.plugin.connection.velocity.platform.VelocityPlatformImpl
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.KickedFromServerEvent
import kotlin.jvm.optionals.getOrNull

class KickedFromServerListener(
    private val plugin: VelocityConnectionPlugin,
    private val platform: VelocityPlatformImpl,
) {

    @Subscribe(priority = 1000)
    fun onKickedFromServer(event: KickedFromServerEvent) {
        val config = plugin.connectionPlugin.connectionConfig.get()
        val messages = plugin.connectionPlugin.messageConfig.get()

        if (!config.fallback.enabled) return
        if (event.kickedDuringServerConnect() && event.player.currentServer.getOrNull() != null) return

        val targetServer = ConnectionResolver.findFallbackTarget(
            platform,
            VelocityConnectionPlayer(event.player),
            event.server.serverInfo.name,
            config,
        )

        event.result = if (targetServer != null) {
            KickedFromServerEvent.RedirectPlayer.create(targetServer.server)
        } else {
            KickedFromServerEvent.DisconnectPlayer.create(messages.msg(messages.kick.noFallbackServers))
        }
    }
}
