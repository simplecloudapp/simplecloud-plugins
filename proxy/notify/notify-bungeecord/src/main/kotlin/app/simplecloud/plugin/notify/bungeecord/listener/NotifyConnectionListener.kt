package app.simplecloud.plugin.notify.bungeecord.listener

import app.simplecloud.plugin.notify.shared.NotifyPlugin
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class NotifyConnectionListener(
    private val notify: NotifyPlugin,
) : Listener {

    @EventHandler
    fun onPostLogin(event: PostLoginEvent) {
        notify.onPlayerJoin(event.player.uniqueId)
    }

    @EventHandler
    fun onDisconnect(event: PlayerDisconnectEvent) {
        notify.onPlayerQuit(event.player.uniqueId)
    }

}