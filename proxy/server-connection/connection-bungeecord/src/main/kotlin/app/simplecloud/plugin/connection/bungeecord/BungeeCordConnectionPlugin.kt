package app.simplecloud.plugin.connection.bungeecord

import app.simplecloud.plugin.connection.bungeecord.listener.ConnectionListener
import app.simplecloud.plugin.connection.bungeecord.platform.BungeeCordPlatformImpl
import app.simplecloud.plugin.connection.shared.ConnectionPlugin
import net.md_5.bungee.api.plugin.Plugin

class BungeeCordConnectionPlugin : Plugin() {

    val connectionPlugin = ConnectionPlugin(BungeeCordPlatformImpl(this))

    override fun onEnable() {
        proxy.pluginManager.registerListener(this, ConnectionListener(connectionPlugin, proxy))
        connectionPlugin.start()
    }

    override fun onDisable() {
        connectionPlugin.shutdown()
    }
}
