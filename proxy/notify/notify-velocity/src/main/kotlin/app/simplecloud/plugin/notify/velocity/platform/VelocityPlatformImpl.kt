package app.simplecloud.plugin.notify.velocity.platform

import app.simplecloud.plugin.core.shared.command.CloudSender
import app.simplecloud.plugin.core.velocity.command.VelocityCloudSender
import app.simplecloud.plugin.notify.shared.platform.NotifyPlatform
import com.velocitypowered.api.proxy.ProxyServer
import java.io.File
import java.nio.file.Path

class VelocityPlatformImpl(
    private val dataDirectory: Path,
    private val server: ProxyServer,
) : NotifyPlatform {

    override fun getDataDirectory(): File = dataDirectory.toFile()

    override fun getOnlinePlayers(): Collection<CloudSender> = server.allPlayers.map(::VelocityCloudSender)
}
