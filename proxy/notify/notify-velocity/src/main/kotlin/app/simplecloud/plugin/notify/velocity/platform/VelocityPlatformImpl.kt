package app.simplecloud.plugin.notify.velocity.platform

import app.simplecloud.plugin.notify.shared.command.CloudSender
import app.simplecloud.plugin.notify.shared.platform.NotifyPlatform
import app.simplecloud.plugin.notify.velocity.command.VelocityCloudSender
import com.velocitypowered.api.proxy.ProxyServer
import java.io.File
import java.nio.file.Path

class VelocityPlatformImpl(
    private val dataDirectory: Path,
    private val server: ProxyServer
) : NotifyPlatform {

    override fun getDataDirectory(): File {
        return dataDirectory.toFile()
    }

    override fun getOnlinePlayers(): Collection<CloudSender> {
        return server.allPlayers.map(::VelocityCloudSender)
    }
}
