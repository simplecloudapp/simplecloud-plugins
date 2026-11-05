package app.simplecloud.plugin.connection.bungeecord

import app.simplecloud.plugin.connection.bungeecord.command.BungeeCordCommandManager
import app.simplecloud.plugin.connection.bungeecord.command.ConnectionCommand
import app.simplecloud.plugin.connection.bungeecord.listener.ConnectionListener
import app.simplecloud.plugin.connection.bungeecord.platform.BungeeCordPlatformImpl
import app.simplecloud.plugin.connection.shared.ConnectionPlugin
import net.md_5.bungee.api.plugin.Plugin

class BungeeCordConnectionPlugin : Plugin() {

    private val commandManager = BungeeCordCommandManager(this)
    private val platform = BungeeCordPlatformImpl(this)

    val connectionPlugin = ConnectionPlugin(platform)

    override fun onEnable() {
        cleanupServers()
        registerListeners()
        registerCommands()
        connectionPlugin.start(::reload)
    }

    override fun onDisable() {
        commandManager.unregisterCommands()
        connectionPlugin.shutdown()
    }

    fun reload() {
        connectionPlugin.reload()
        commandManager.unregisterCommands()
        commandManager.registerCommands()
    }

    private fun cleanupServers() {
        if (connectionPlugin.connectionConfig.get().registration.enabled) {
            platform.getRegistry().clear()
        }
    }

    private fun registerListeners() {
        proxy.pluginManager.registerListener(this, ConnectionListener(this))
    }

    private fun registerCommands() {
        commandManager.registerCommands()
        proxy.pluginManager.registerCommand(this, ConnectionCommand(this))
    }
}
