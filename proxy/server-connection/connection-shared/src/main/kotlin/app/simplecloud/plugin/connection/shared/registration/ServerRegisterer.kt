package app.simplecloud.plugin.connection.shared.registration

import app.simplecloud.api.CloudApi
import app.simplecloud.api.cache.QueryKey
import app.simplecloud.api.event.Subscription
import app.simplecloud.api.group.GroupServerType
import app.simplecloud.api.internal.CloudApiImpl
import app.simplecloud.api.proxy.ProxyServerNames
import app.simplecloud.api.server.Server
import app.simplecloud.api.server.ServerQuery
import app.simplecloud.api.server.ServerState
import app.simplecloud.api.shaded.nats.client.Connection
import app.simplecloud.plugin.connection.shared.config.ConnectionConfig
import app.simplecloud.plugin.connection.shared.utilities.RegisteredServerResolver
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.seconds

class ServerRegisterer(
    private val api: CloudApi,
    registry: ServerRegistry,
    private val config: ConfigurationFactory<ConnectionConfig>,
) {

    private val logger = LoggerFactory.getLogger(ServerRegisterer::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val subscriptions: MutableList<Subscription> = mutableListOf()
    private val registrations = ProxyServerRegistrations(registry)
    private val lookupRegistration = AtomicReference<ProxyServerNames.Registration?>()
    private val stoppingServers = ConcurrentHashMap.newKeySet<String>()
    private val syncMutex = Mutex()
    private val syncRequired = AtomicBoolean(true)
    private val changedDuringSync = ConcurrentHashMap.newKeySet<String>()

    fun start() {
        lookupRegistration.set(ProxyServerNames.register(registrations))
        subscriptions.add(
            api.event().server().onStateChanged { event ->
                val server = event.server ?: return@onStateChanged
                if (server.serverBase?.type != GroupServerType.SERVER) return@onStateChanged
                if (event.newState == ServerState.STOPPING || event.newState == ServerState.CLEANUP) {
                    stoppingServers.add(server.serverId)
                }
                if (event.newState == ServerState.AVAILABLE && event.oldState != ServerState.AVAILABLE) {
                    stoppingServers.remove(server.serverId)
                    markChanged(server.serverId)
                    scope.launch {
                        register(server)
                    }
                }
                if (event.newState == ServerState.STOPPED) {
                    markChanged(server.serverId)
                    scope.launch {
                        unregister(server.serverId)
                    }
                }
            },
        )

        subscriptions.add(
            api.event().server().onUpdated { event ->
                val previousRegistration = registrations.find(event.serverId) ?: return@onUpdated
                val server = event.server ?: return@onUpdated
                scope.launch {
                    val registeredServer = convertToRegisteredServer(server) ?: return@launch
                    if (registeredServer.proxyName != previousRegistration.proxyName) {
                        markChanged(registeredServer.serverId)
                        register(registeredServer)
                    }
                }
            },
        )

        subscriptions.add(
            api.event().server().onStopped { event ->
                val server = event.server ?: return@onStopped
                markChanged(server.serverId)
                scope.launch {
                    unregister(server.serverId)
                }
            },
        )

        subscriptions.add(
            api.event().server().onDeleted { event ->
                markChanged(event.serverId)
                scope.launch {
                    unregister(event.serverId)
                }
            },
        )

        scope.launch {
            watchConnection()
        }
    }

    fun stop() {
        lookupRegistration.getAndSet(null)?.close()
        subscriptions.forEach { subscription -> subscription.close() }
        subscriptions.clear()
        scope.cancel()
    }

    fun getStoppingServerNames(): Set<String> = registrations.findAll()
        .filter { server -> server.serverId in stoppingServers }
        .map { server -> server.proxyName }
        .toSet()

    fun register(server: Server) {
        val registeredServer = convertToRegisteredServer(server) ?: return
        register(registeredServer)
    }

    suspend fun refresh() {
        registrations.findAll().forEach { server ->
            register(server.copy(proxyName = resolveProxyName(server)))
        }
        sync()
    }

    private suspend fun watchConnection() {
        while (currentCoroutineContext().isActive) {
            if (!isConnected()) {
                if (!syncRequired.getAndSet(true)) {
                    logger.warn("Lost connection to SimpleCloud, the servers will be loaded again once it is back")
                }
            } else if (syncRequired.get() && !sync()) {
                delay(10.seconds)
            }
            delay(1.seconds)
        }
    }

    private fun isConnected(): Boolean = (api as CloudApiImpl).natsConnection.status == Connection.Status.CONNECTED

    private suspend fun sync(): Boolean = syncMutex.withLock {
        syncRequired.set(!isConnected())
        changedDuringSync.clear()
        try {
            api.cache().invalidateAll(QueryKey.of("servers"))
            val servers = fetchRunningServers()
            logger.info("Found ${servers.size} servers")

            val serverIds = servers.map { server -> server.serverId }.toSet()
            synchronized(changedDuringSync) {
                registrations.findAll()
                    .map { server -> server.serverId }
                    .filter { serverId -> serverId !in serverIds && serverId !in changedDuringSync }
                    .forEach { serverId -> unregister(serverId) }
                servers
                    .filter { server -> server.serverId !in changedDuringSync }
                    .forEach { server -> register(server) }
            }
            true
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            syncRequired.set(true)
            logger.warn("Could not load the servers from SimpleCloud, retrying: ${e.message}")
            false
        }
    }

    private fun markChanged(serverId: String) {
        synchronized(changedDuringSync) {
            if (syncMutex.isLocked) {
                changedDuringSync.add(serverId)
            }
        }
    }

    private suspend fun fetchRunningServers(): List<Server> = api.server().getAllServers(
        ServerQuery.create()
            .filterByState(ServerState.AVAILABLE, ServerState.INGAME)
            .filterByServerGroupType(GroupServerType.SERVER),
    ).await()

    private fun register(server: RegisteredServer) {
        if (server.blueprintConfigurator == "standalone") return

        val ignoredNames = config.get().registration.ignoreServerGroupsAndPersistentServers
        if (ignoredNames.any { name -> name.equals(server.serverBaseName, ignoreCase = true) }) {
            unregister(server.serverId)
            return
        }

        registrations.register(server)
    }

    private fun unregister(serverId: String) {
        stoppingServers.remove(serverId)
        registrations.unregister(serverId)
    }

    private fun convertToRegisteredServer(server: Server): RegisteredServer? {
        val serverBaseName = server.serverBase?.name
        val ip = server.ip
        val port = server.port
        if (serverBaseName == null || ip == null || port == null) {
            logger.warn("Skipping server ${server.serverId} because its server base name, ip or port is missing.")
            return null
        }

        val registeredServer = RegisteredServer(
            serverId = server.serverId,
            numericalId = server.numericalId,
            ip = ip,
            port = port,
            serverBaseName = serverBaseName,
            proxyName = "",
            properties = server.properties ?: emptyMap(),
            blueprintConfigurator = server.blueprint?.configurator,
            persistent = server.isFromPersistentServer,
        )
        return registeredServer.copy(proxyName = resolveProxyName(registeredServer))
    }

    private fun resolveProxyName(server: RegisteredServer): String = RegisteredServerResolver.resolve(
        serverId = server.serverId,
        numericalId = server.numericalId,
        serverBaseName = server.serverBaseName,
        properties = server.properties,
        persistent = server.persistent,
        config = config.get().registration,
    )
}
