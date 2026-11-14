package app.simplecloud.plugin.connection.shared.registration

import app.simplecloud.api.CloudApi
import app.simplecloud.api.cache.QueryKey
import app.simplecloud.api.event.Subscription
import app.simplecloud.api.event.server.ServerStateChangedEvent
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
    private val registrations = ServerRegistrations(registry)
    private val lookupRegistration = AtomicReference<ProxyServerNames.Registration?>()
    private val statuses = ConcurrentHashMap<String, ServerStatus>()
    private val mutex = Mutex()
    private val syncRequired = AtomicBoolean(true)
    private val playerCountsFailing = AtomicBoolean(false)

    fun start() {
        lookupRegistration.set(ProxyServerNames.register(registrations))

        subscriptions.add(
            api.event().server().onStateChanged { event ->
                try {
                    onStateChanged(event)
                } catch (e: IllegalStateException) {
                    logger.debug("Ignoring state change of server ${event.serverId}: ${e.message}")
                }
            },
        )

        subscriptions.add(
            api.event().server().onUpdated { event ->
                if (registrations.find(event.serverId) == null) return@onUpdated
                val server = event.server ?: return@onUpdated
                scope.launch {
                    mutex.withLock { update(server) }
                }
            },
        )

        subscriptions.add(
            api.event().server().onStopped { event ->
                scope.launch {
                    mutex.withLock { unregister(event.serverId) }
                }
            },
        )

        subscriptions.add(
            api.event().server().onDeleted { event ->
                scope.launch {
                    mutex.withLock { unregister(event.serverId) }
                }
            },
        )

        scope.launch {
            watchConnection()
        }

        scope.launch {
            watchPlayerCounts()
        }
    }

    fun stop() {
        lookupRegistration.getAndSet(null)?.close()
        subscriptions.forEach { subscription -> subscription.close() }
        subscriptions.clear()
        scope.cancel()
    }

    fun getServerStatuses(): Map<String, ServerStatus> = registrations.findAll()
        .filter { server -> registrations.proxyNameOf(server.serverId).isPresent }
        .associate { server -> server.proxyName to (statuses[server.serverId] ?: ServerStatus(false, 0, 0)) }

    suspend fun refresh() {
        mutex.withLock {
            registrations.findAll().forEach { server ->
                register(server.copy(proxyName = resolveProxyName(server)))
            }
        }
        sync()
    }

    private fun onStateChanged(event: ServerStateChangedEvent) {
        if (event.newState == ServerState.STOPPING || event.newState == ServerState.CLEANUP) {
            markStopping(event.serverId)
        }
        val becameAvailable = event.newState == ServerState.AVAILABLE && event.oldState != ServerState.AVAILABLE
        if (!becameAvailable) return

        val server = event.server ?: return
        scope.launch {
            mutex.withLock { register(server, false) }
        }
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

    private suspend fun watchPlayerCounts() {
        while (currentCoroutineContext().isActive) {
            delay(config.get().registration.playerCountUpdateInterval.coerceAtLeast(1).seconds)
            if (isConnected()) {
                updatePlayerCounts()
            }
        }
    }

    private fun isConnected(): Boolean = (api as CloudApiImpl).natsConnection.status == Connection.Status.CONNECTED

    private suspend fun sync(): Boolean = mutex.withLock {
        syncRequired.set(!isConnected())
        try {
            api.cache().invalidateAll(QueryKey.of("servers"))
            val servers = fetchServers()
            logger.info("Found ${servers.size} servers")

            val serverIds = servers.map { server -> server.serverId }.toSet()
            registrations.findAll()
                .filter { server -> server.serverId !in serverIds }
                .forEach { server -> unregister(server.serverId) }
            servers.forEach { server -> register(server, isStopping(server)) }
            true
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            syncRequired.set(true)
            logger.warn("Could not load the servers from SimpleCloud, retrying: ${e.message}")
            false
        }
    }

    private suspend fun updatePlayerCounts() {
        try {
            api.cache().invalidateAll(QueryKey.of("servers"))
            fetchServers().forEach { server ->
                statuses.computeIfPresent(server.serverId) { _, status ->
                    status.copy(
                        stopping = status.stopping || isStopping(server),
                        playerCount = server.playerCount ?: 0,
                        maxPlayers = maxPlayersOf(server),
                    )
                }
            }
            if (playerCountsFailing.getAndSet(false)) {
                logger.info("Loading the player counts from SimpleCloud works again")
            }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            if (!playerCountsFailing.getAndSet(true)) {
                logger.warn("Could not load the player counts from SimpleCloud, retrying: ${e.message}")
            }
        }
    }

    private suspend fun fetchServers(): List<Server> = api.server().getAllServers(
        ServerQuery.create()
            .filterByState(ServerState.AVAILABLE, ServerState.INGAME, ServerState.STOPPING, ServerState.CLEANUP)
            .filterByServerGroupType(GroupServerType.SERVER),
    ).await()

    private fun register(server: Server, stopping: Boolean) {
        val registeredServer = convertToRegisteredServer(server) ?: return
        statuses[server.serverId] = ServerStatus(stopping, server.playerCount ?: 0, maxPlayersOf(server))
        register(registeredServer)
    }

    private fun register(server: RegisteredServer) {
        val ignoredNames = config.get().registration.ignoreServerGroupsAndPersistentServers
        if (ignoredNames.any { name -> name.equals(server.serverBaseName, ignoreCase = true) }) {
            unregister(server.serverId)
            return
        }

        registrations.register(server)
    }

    private fun update(server: Server) {
        val previousServer = registrations.find(server.serverId) ?: return
        val registeredServer = convertToRegisteredServer(server) ?: return
        if (registeredServer != previousServer) {
            register(registeredServer)
        }
    }

    private fun unregister(serverId: String) {
        statuses.remove(serverId)
        registrations.unregister(serverId)
    }

    private fun markStopping(serverId: String) {
        statuses.computeIfPresent(serverId) { _, status -> status.copy(stopping = true) }
    }

    private fun isStopping(server: Server): Boolean =
        server.state == ServerState.STOPPING || server.state == ServerState.CLEANUP

    private fun maxPlayersOf(server: Server): Int = try {
        server.maxPlayers
    } catch (_: IllegalStateException) {
        0
    }

    private fun convertToRegisteredServer(server: Server): RegisteredServer? = try {
        val serverBase = server.serverBase
        val ip = server.ip
        val port = server.port
        when {
            serverBase.type != GroupServerType.SERVER -> null

            ip == null || port == null -> {
                logger.warn("Skipping server ${server.serverId} because its ip or port is missing.")
                null
            }

            else -> {
                val registeredServer = RegisteredServer(
                    serverId = server.serverId,
                    numericalId = server.numericalId,
                    ip = ip,
                    port = port,
                    serverBaseName = serverBase.name,
                    proxyName = "",
                    properties = server.properties ?: emptyMap(),
                    persistent = server.isFromPersistentServer,
                )
                registeredServer.copy(proxyName = resolveProxyName(registeredServer))
            }
        }
    } catch (e: IllegalStateException) {
        logger.warn("Skipping server ${server.serverId}: ${e.message}")
        null
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
