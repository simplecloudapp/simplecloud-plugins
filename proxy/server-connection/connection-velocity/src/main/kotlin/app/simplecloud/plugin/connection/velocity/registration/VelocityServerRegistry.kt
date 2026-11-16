package app.simplecloud.plugin.connection.velocity.registration

import app.simplecloud.plugin.connection.shared.registration.RegisteredServer
import app.simplecloud.plugin.connection.shared.registration.ServerRegistry
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.ServerInfo
import java.net.InetSocketAddress
import kotlin.jvm.optionals.getOrNull

class VelocityServerRegistry(
    private val proxy: ProxyServer,
) : ServerRegistry {

    override fun register(proxyName: String, server: RegisteredServer) {
        val serverInfo = ServerInfo(proxyName, InetSocketAddress.createUnresolved(server.ip, server.port))
        val existing = proxy.getServer(proxyName).getOrNull()
        if (existing?.serverInfo == serverInfo) return

        if (existing != null) proxy.unregisterServer(existing.serverInfo)
        proxy.registerServer(serverInfo)
    }

    override fun unregister(proxyName: String, server: RegisteredServer) {
        val existing = proxy.getServer(proxyName).getOrNull() ?: return
        if (existing.serverInfo.address != InetSocketAddress.createUnresolved(server.ip, server.port)) return
        proxy.unregisterServer(existing.serverInfo)
    }

    override fun clear() {
        proxy.allServers.forEach { server -> proxy.unregisterServer(server.serverInfo) }
    }
}
