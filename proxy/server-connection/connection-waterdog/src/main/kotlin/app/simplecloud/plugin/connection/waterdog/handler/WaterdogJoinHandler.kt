package app.simplecloud.plugin.connection.waterdog.handler

import app.simplecloud.plugin.connection.shared.connection.ConnectionResolver
import app.simplecloud.plugin.connection.waterdog.WaterdogConnectionPlugin
import app.simplecloud.plugin.connection.waterdog.platform.WaterdogConnectionPlayer
import app.simplecloud.plugin.connection.waterdog.platform.WaterdogPlatformImpl
import dev.waterdog.waterdogpe.network.connection.handler.IJoinHandler
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo
import dev.waterdog.waterdogpe.player.ProxiedPlayer

class WaterdogJoinHandler(
    private val plugin: WaterdogConnectionPlugin,
    private val platform: WaterdogPlatformImpl,
    private val defaultHandler: IJoinHandler,
) : IJoinHandler {

    override fun determineServer(player: ProxiedPlayer): ServerInfo? {
        val config = plugin.connectionPlugin.connectionConfig.get()

        val targetServer = ConnectionResolver.findJoinTarget(platform, WaterdogConnectionPlayer(player), config)
        if (targetServer != null) return targetServer.serverInfo
        if (!config.networkJoinTargets.enabled) return defaultHandler.determineServer(player)

        return null
    }
}
