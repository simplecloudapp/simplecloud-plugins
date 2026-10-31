package app.simplecloud.plugin.connection.shared.registration

import app.simplecloud.api.proxy.ProxyServerNames
import org.slf4j.LoggerFactory
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

/**
 * The cloud servers registered on this proxy, and which of them holds each proxy name.
 *
 * Several servers can resolve to the same proxy name, and registering a name on the proxy replaces the server that
 * had it. Tracking the holder keeps a replaced server from removing the holder when it stops, and lets the
 * [ProxyServerNames.Lookup] answer only for the holder, so a connect to a replaced server's ID answers "not found"
 * instead of reaching the server that replaced it.
 */
class ProxyServerRegistrations(
    private val registry: ServerRegistry,
) : ProxyServerNames.Lookup {

    private val logger = LoggerFactory.getLogger(ProxyServerRegistrations::class.java)

    // Keeps holderIds in step with the proxy while events, resyncs and reloads register servers concurrently.
    private val lock = Any()
    private val servers = ConcurrentHashMap<String, RegisteredServer>()

    // Keyed by the lowercased proxy name, because the proxies compare server names ignoring case.
    private val holderIds = ConcurrentHashMap<String, String>()

    fun find(serverId: String): RegisteredServer? = servers[serverId]

    fun findAll(): List<RegisteredServer> = servers.values.toList()

    fun register(server: RegisteredServer) {
        synchronized(lock) {
            val previousServer = servers[server.serverId]

            if (previousServer != null && nameKeyOf(previousServer.proxyName) == nameKeyOf(server.proxyName)) {
                logger.info("Refreshing server ${server.serverId} (${server.proxyName})...")
            } else {
                if (previousServer != null) {
                    logger.info("Replacing stale registration ${previousServer.serverId} (${previousServer.proxyName}) with ${server.proxyName}...")
                    servers.remove(server.serverId)
                    releaseProxyName(previousServer)
                }
                logger.info("Registering server ${server.serverId} (${server.proxyName})...")
            }

            registerAsHolder(server)
            servers[server.serverId] = server
        }
    }

    fun unregister(serverId: String) {
        synchronized(lock) {
            val registeredServer = servers.remove(serverId) ?: return
            logger.info("Unregistering server $serverId (${registeredServer.proxyName})...")
            releaseProxyName(registeredServer)
        }
    }

    /**
     * cloud-api calls this on the NATS dispatcher thread, so it must not block: it only reads the concurrent maps.
     */
    override fun proxyNameOf(serverId: String): Optional<String> {
        val server = servers[serverId] ?: return Optional.empty()
        val holderId = holderIds[nameKeyOf(server.proxyName)]
        if (holderId != serverId) return Optional.empty()
        return Optional.of(server.proxyName)
    }

    // When the holder changes, the lookup answers for neither server while the proxy switches the name over, so it
    // doesn't answer for a server before this class has switched the proxy to it.
    private fun registerAsHolder(server: RegisteredServer) {
        val nameKey = nameKeyOf(server.proxyName)
        val previousHolderId = holderIds[nameKey]
        if (previousHolderId != server.serverId) {
            holderIds.remove(nameKey)
        }
        if (previousHolderId != null && previousHolderId != server.serverId) {
            logger.warn("Server ${server.serverId} replaces server $previousHolderId as ${server.proxyName} on the proxy")
        }
        registry.register(server.proxyName, server)
        holderIds[nameKey] = server.serverId
    }

    // Hands the name over to another server with the same name, or removes it from the proxy if there is none.
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
