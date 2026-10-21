package app.simplecloud.plugin.connection.waterdog.platform

import app.simplecloud.plugin.connection.shared.platform.ConnectionPlatform
import app.simplecloud.plugin.connection.waterdog.registration.WaterdogServerRegistry
import dev.waterdog.waterdogpe.plugin.Plugin
import java.io.File

class WaterdogPlatformImpl(
    private val plugin: Plugin,
) : ConnectionPlatform<WaterdogConnectionServer> {

    override fun getDataDirectory(): File = plugin.dataFolder

    override fun getRegistry(): WaterdogServerRegistry = WaterdogServerRegistry(plugin.proxy)

    override fun getServers(): List<WaterdogConnectionServer> = plugin.proxy.servers.map { serverInfo ->
        WaterdogConnectionServer(serverInfo)
    }
}
