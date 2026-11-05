package app.simplecloud.plugin.connection.waterdog

import app.simplecloud.plugin.connection.shared.ConnectionPlugin
import app.simplecloud.plugin.connection.waterdog.command.ConnectionCommand
import app.simplecloud.plugin.connection.waterdog.command.WaterdogCommandManager
import app.simplecloud.plugin.connection.waterdog.handler.WaterdogJoinHandler
import app.simplecloud.plugin.connection.waterdog.handler.WaterdogReconnectHandler
import app.simplecloud.plugin.connection.waterdog.platform.WaterdogConnectionServer
import app.simplecloud.plugin.connection.waterdog.platform.WaterdogPlatformImpl
import dev.waterdog.waterdogpe.plugin.Plugin

class WaterdogConnectionPlugin : Plugin() {

    private val commandManager = WaterdogCommandManager(this)
    private val platform = WaterdogPlatformImpl(this)

    var connectionPlugin: ConnectionPlugin<WaterdogConnectionServer>? = null
        private set

    override fun onEnable() {
        val connectionPlugin = ConnectionPlugin(platform)
        this.connectionPlugin = connectionPlugin
        cleanupServers(connectionPlugin)
        registerHandlers()
        registerCommands()
        connectionPlugin.start(::reload)
    }

    override fun onDisable() {
        commandManager.unregisterCommands()
        connectionPlugin?.shutdown()
    }

    fun reload() {
        connectionPlugin?.reload()
        commandManager.unregisterCommands()
        commandManager.registerCommands()
    }

    private fun cleanupServers(connectionPlugin: ConnectionPlugin<WaterdogConnectionServer>) {
        if (connectionPlugin.connectionConfig.get().registration.enabled) {
            proxy.servers.forEach { serverInfo ->
                proxy.removeServerInfo(serverInfo.serverName)
            }
        }
    }

    private fun registerHandlers() {
        proxy.joinHandler = WaterdogJoinHandler(this, proxy.joinHandler)
        proxy.reconnectHandler = WaterdogReconnectHandler(this, proxy.reconnectHandler)
    }

    private fun registerCommands() {
        commandManager.registerCommands()
        proxy.commandMap.registerCommand(ConnectionCommand(this))
    }
}
