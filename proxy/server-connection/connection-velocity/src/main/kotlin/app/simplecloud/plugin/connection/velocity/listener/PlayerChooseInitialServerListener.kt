package app.simplecloud.plugin.connection.velocity.listener

import app.simplecloud.plugin.connection.shared.connection.ConnectionResolver
import app.simplecloud.plugin.connection.velocity.VelocityConnectionPlugin
import app.simplecloud.plugin.connection.velocity.platform.VelocityConnectionPlayer
import app.simplecloud.plugin.connection.velocity.platform.VelocityPlatformImpl
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent

class PlayerChooseInitialServerListener(
    private val plugin: VelocityConnectionPlugin,
    private val platform: VelocityPlatformImpl,
) {

    @Subscribe
    fun onPlayerChooseInitialServer(event: PlayerChooseInitialServerEvent) {
        val config = plugin.connectionPlugin.connectionConfig.get()
        val messages = plugin.connectionPlugin.messageConfig.get()

        val targetServer = ConnectionResolver.findJoinTarget(platform, VelocityConnectionPlayer(event.player), config)
        if (targetServer != null) {
            event.setInitialServer(targetServer.server)
            return
        }

        if (!config.networkJoinTargets.enabled) return

        event.setInitialServer(null)
        event.player.disconnect(messages.msg(messages.kick.noTargetConnection))
    }
}
