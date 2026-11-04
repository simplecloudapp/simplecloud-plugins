package app.simplecloud.plugin.connection.shared.registration

import app.simplecloud.api.proxy.ProxyServerNames
import org.slf4j.LoggerFactory
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

class ProxyServerRegistrations(
    private val registry: ServerRegistry,
) : ProxyServerNames.Lookup {

    private val logger = LoggerFactory.getLogger(ProxyServerRegistrations::class.java)

    private val lock = Any()
    private val servers = ConcurrentHashMap<String, RegisteredServer>()
    private val holderIds = ConcurrentHashMap<String, String>()

    fun find(serverId: String): RegisteredServer? = servers[serverId]

    fun findAll(): List<RegisteredServer> = servers.values.toList()

    fun register(server: RegisteredServer) {
        synchronized(lock) {
            val previousServer = servers[server.serverId]
            when {
                previousServer == null -> registerNewServer(server)
                changesProxyName(previousServer, server) -> moveToNewProxyName(previousServer, server)
                else -> refreshServer(server)
            }
        }
    }

    fun unregister(serverId: String) {
        synchronized(lock) {
            val registeredServer = servers.remove(serverId) ?: return
            logger.info("Unregistering server $serverId (${registeredServer.proxyName})...")
            releaseProxyName(registeredServer)
        }
    }

    override fun proxyNameOf(serverId: String): Optional<String> {
        val server = servers[serverId] ?: return Optional.empty()
        val holderId = holderIds[nameKeyOf(server.proxyName)]
        if (holderId != serverId) return Optional.empty()
        return Optional.of(server.proxyName)
    }

    private fun registerNewServer(server: RegisteredServer) {
        logger.info("Registering server ${server.serverId} (${server.proxyName})...")
        registerAsHolder(server)
        servers[server.serverId] = server
    }

    private fun changesProxyName(previousServer: RegisteredServer, server: RegisteredServer): Boolean =
        nameKeyOf(previousServer.proxyName) != nameKeyOf(server.proxyName)

    private fun moveToNewProxyName(previousServer: RegisteredServer, server: RegisteredServer) {
        logger.info("Replacing stale registration ${previousServer.serverId} (${previousServer.proxyName}) with ${server.proxyName}...")
        servers.remove(server.serverId)
        releaseProxyName(previousServer)
        registerNewServer(server)
    }

    private fun refreshServer(server: RegisteredServer) {
        val holderId = holderIds[nameKeyOf(server.proxyName)]
        val nameHeldByOther = holderId != null && holderId != server.serverId
        if (nameHeldByOther) {
            logger.info("Refreshing server ${server.serverId} (${server.proxyName}), server $holderId keeps the name...")
            servers[server.serverId] = server
            return
        }

        logger.info("Refreshing server ${server.serverId} (${server.proxyName})...")
        registerAsHolder(server)
        servers[server.serverId] = server
    }

    private fun registerAsHolder(server: RegisteredServer) {
        val nameKey = nameKeyOf(server.proxyName)
        val previousHolderId = holderIds[nameKey]
        val holderChanges = previousHolderId != server.serverId
        if (holderChanges) {
            holderIds.remove(nameKey)
        }
        if (holderChanges && previousHolderId != null) {
            logger.warn("Server ${server.serverId} replaces server $previousHolderId as ${server.proxyName} on the proxy")
        }
        registry.register(server.proxyName, server)
        holderIds[nameKey] = server.serverId
    }

    private fun releaseProxyName(server: RegisteredServer) {
        val nameKey = nameKeyOf(server.proxyName)
        if (holderIds[nameKey] != server.serverId) return
        holderIds.remove(nameKey)

        val successor = servers.values.firstOrNull { other -> nameKeyOf(other.proxyName) == nameKey }
        if (successor == null) {
            registry.unregister(server.proxyName, server)
            return
        }

        logger.info("Handing ${server.proxyName} over to server ${successor.serverId}")
        registerAsHolder(successor)
    }

    private fun nameKeyOf(proxyName: String): String = proxyName.lowercase()
}
