package app.simplecloud.plugin.connection.waterdog.handler

import app.simplecloud.plugin.connection.waterdog.WaterdogConnectionPlugin
import app.simplecloud.plugin.connection.waterdog.platform.WaterdogConnectionPlayer
import dev.waterdog.waterdogpe.network.connection.handler.IJoinHandler
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo
import dev.waterdog.waterdogpe.player.ProxiedPlayer

class WaterdogJoinHandler(
    private val plugin: WaterdogConnectionPlugin,
    private val defaultHandler: IJoinHandler,
) : IJoinHandler {

    override fun determineServer(player: ProxiedPlayer): ServerInfo? {
        val connectionPlugin = plugin.connectionPlugin ?: return defaultHandler.determineServer(player)
        val config = connectionPlugin.connectionConfig.get()

        val targetServer = connectionPlugin.findJoinTarget(WaterdogConnectionPlayer(player))
        if (targetServer != null) return targetServer.serverInfo
        if (!config.networkJoinTargets.enabled) return defaultHandler.determineServer(player)

        return null
    }
}
