package app.simplecloud.plugin.connection.bungeecord.registration

import app.simplecloud.plugin.connection.shared.registration.RegisteredServer
import app.simplecloud.plugin.connection.shared.registration.ServerRegistry
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo
import java.net.InetSocketAddress

class BungeeCordServerRegistry(
    private val proxy: ProxyServer,
) : ServerRegistry {

    private val lock = Any()

    fun findServers(): List<ServerInfo> = synchronized(lock) {
        proxy.servers.values.toList()
    }

    override fun register(proxyName: String, server: RegisteredServer) {
        val address = InetSocketAddress.createUnresolved(server.ip, server.port)
        val info: ServerInfo = proxy.constructServerInfo(
            proxyName,
            address,
            server.serverId,
            server.properties.getOrDefault("proxy-restricted", "false").toString().toBoolean(),
        )

        synchronized(lock) {
            if (isUnchanged(proxy.servers[proxyName], info)) return
            proxy.servers[proxyName] = info
            refreshServerPriorities()
        }
    }

    override fun unregister(proxyName: String, server: RegisteredServer) {
        val address = InetSocketAddress.createUnresolved(server.ip, server.port)
        synchronized(lock) {
            if (proxy.servers[proxyName]?.socketAddress != address) return
            proxy.servers.remove(proxyName)
            refreshServerPriorities()
        }
    }

    override fun clear() {
        synchronized(lock) {
            proxy.servers.clear()
            proxy.config.listeners.forEach { listener -> listener.serverPriority.clear() }
        }
    }

    private fun isUnchanged(existing: ServerInfo?, info: ServerInfo): Boolean {
        if (existing == null) return false
        return existing.name == info.name &&
            existing.socketAddress == info.socketAddress &&
            existing.isRestricted == info.isRestricted &&
            existing.motd == info.motd
    }

    private fun refreshServerPriorities() {
        val serverNames = proxy.servers.keys.toList()
        proxy.config.listeners.forEach { listener ->
            listener.serverPriority.removeIf { priority ->
                serverNames.none { name -> name.equals(priority, ignoreCase = true) }
            }
            serverNames.forEach { serverName ->
                if (listener.serverPriority.none { priority -> priority.equals(serverName, ignoreCase = true) }) {
                    listener.serverPriority.add(serverName)
                }
            }
        }
    }
}
