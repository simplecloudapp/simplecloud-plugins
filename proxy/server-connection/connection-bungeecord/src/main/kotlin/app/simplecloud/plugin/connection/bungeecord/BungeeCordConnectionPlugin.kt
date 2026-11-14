package app.simplecloud.plugin.connection.bungeecord

import app.simplecloud.plugin.connection.bungeecord.listener.ConnectionListener
import app.simplecloud.plugin.connection.bungeecord.platform.BungeeCordPlatformImpl
import app.simplecloud.plugin.connection.shared.ConnectionPlugin
import net.md_5.bungee.api.plugin.Plugin

class BungeeCordConnectionPlugin : Plugin() {

    val plugin = ConnectionPlugin(BungeeCordPlatformImpl(this))

    override fun onEnable() {
        proxy.pluginManager.registerListener(this, ConnectionListener(plugin, proxy))
        plugin.start()
    }

    override fun onDisable() {
        plugin.shutdown()
    }
}
