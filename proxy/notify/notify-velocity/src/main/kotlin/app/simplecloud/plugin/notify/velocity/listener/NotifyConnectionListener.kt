package app.simplecloud.plugin.notify.velocity.listener

import app.simplecloud.plugin.notify.shared.NotifyPlugin
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent

class NotifyConnectionListener(
    private val notify: NotifyPlugin
) {

    @Subscribe
    fun onPostLogin(event: PostLoginEvent) {
        notify.onPlayerJoin(event.player.uniqueId)
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        notify.onPlayerQuit(event.player.uniqueId)
    }

}