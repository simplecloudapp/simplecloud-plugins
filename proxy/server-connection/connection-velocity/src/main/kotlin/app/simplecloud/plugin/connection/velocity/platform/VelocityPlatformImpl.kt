package app.simplecloud.plugin.connection.velocity.platform

import app.simplecloud.plugin.connection.shared.platform.ConnectionPlatform
import app.simplecloud.plugin.connection.velocity.registration.VelocityServerRegistry
import com.velocitypowered.api.proxy.ProxyServer
import java.io.File
import java.nio.file.Path

class VelocityPlatformImpl(
    private val proxy: ProxyServer,
    private val dataDirectory: Path,
) : ConnectionPlatform<VelocityConnectionServer> {

    private val registry = VelocityServerRegistry(proxy)

    override fun getDataDirectory(): File = dataDirectory.toFile()

    override fun getRegistry(): VelocityServerRegistry = registry

    override fun getServers(): List<VelocityConnectionServer> = proxy.allServers.map { server ->
        VelocityConnectionServer(server)
    }
}
