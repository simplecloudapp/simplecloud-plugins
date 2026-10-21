package app.simplecloud.plugin.connection.velocity.platform

import app.simplecloud.plugin.connection.shared.platform.ConnectionPlatform
import com.velocitypowered.api.proxy.ProxyServer

class VelocityPlatformImpl(
    private val proxy: ProxyServer,
) : ConnectionPlatform<VelocityConnectionServer> {

    override fun getServers(): List<VelocityConnectionServer> = proxy.allServers.map(::VelocityConnectionServer)
}
