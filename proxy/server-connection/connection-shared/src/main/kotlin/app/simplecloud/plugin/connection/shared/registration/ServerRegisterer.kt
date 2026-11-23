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
import app.simplecloud.plugin.connection.shared.config.RegistrationConfig
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
    private val registrations: ServerRegistrations,
    private val cache: ServerStatusCache,
    private val config: ConfigurationFactory<ConnectionConfig>,
) {

    private val logger = LoggerFactory.getLogger(ServerRegisterer::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val subscriptions: MutableList<Subscription> = mutableListOf()
    private val lookupRegistration = AtomicReference<ProxyServerNames.Registration?>()
    private val mutex = Mutex()
    private val outdated = AtomicBoolean(true)
    private val recentEvents = ConcurrentHashMap<String, Long>()
    private val skippedServerIds = ConcurrentHashMap.newKeySet<String>()

    fun start() {
        lookupRegistration.set(ProxyServerNames.register(registrations))

        subscriptions.add(
            api.event().server().onStateChanged { event ->
                recentEvents[event.serverId] = System.currentTimeMillis()
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
                recentEvents[event.serverId] = System.currentTimeMillis()
                try {
                    val server = event.server
                    scope.launch {
                        mutex.withLock { update(server, config.get().registration) }
                    }
                } catch (e: IllegalStateException) {
                    logger.debug("Ignoring update of server ${event.serverId}: ${e.message}")
                }
            },
        )

        subscriptions.add(
            api.event().server().onStopped { event ->
                recentEvents[event.serverId] = System.currentTimeMillis()
                scope.launch {
                    mutex.withLock { unregister(event.serverId) }
                }
            },
        )

        subscriptions.add(
            api.event().server().onDeleted { event ->
                recentEvents[event.serverId] = System.currentTimeMillis()
                scope.launch {
                    mutex.withLock { unregister(event.serverId) }
                }
            },
        )

        scope.launch {
            watchServers()
        }
    }

    fun stop() {
        lookupRegistration.getAndSet(null)?.close()
        subscriptions.forEach { subscription -> subscription.close() }
        subscriptions.clear()
        scope.cancel()
    }

    private fun onStateChanged(event: ServerStateChangedEvent) {
        if (event.newState == ServerState.STOPPING || event.newState == ServerState.CLEANUP) {
            scope.launch {
                mutex.withLock { cache.markStopping(event.serverId) }
            }
        }
        val becameAvailable = event.newState == ServerState.AVAILABLE && event.oldState != ServerState.AVAILABLE
        if (!becameAvailable) return

        val server = event.server
        scope.launch {
            mutex.withLock { register(server, config.get().registration) }
        }
    }

    private suspend fun watchServers() {
        while (currentCoroutineContext().isActive) {
            if (isConnected()) {
                sync()
                delay(config.get().registration.syncInterval.coerceAtLeast(1).seconds)
            } else {
                if (!outdated.getAndSet(true)) {
                    logger.warn("Lost connection to SimpleCloud, the servers will be loaded again once it is back")
                }
                delay(1.seconds)
            }
        }
    }

    private fun isConnected(): Boolean {
        val impl = api as? CloudApiImpl ?: return true
        return impl.natsConnection.status == Connection.Status.CONNECTED
    }

    suspend fun sync() {
        mutex.withLock {
            try {
                val startedAt = System.currentTimeMillis()
                val registration = config.get().registration
                api.cache().invalidateAll(QueryKey.of("servers"))
                val servers = fetchServers().filter { server -> isReadable(server) }
                if (outdated.getAndSet(false)) {
                    logger.info("Found ${servers.size} servers")
                }

                reconcile(servers, registration, startedAt - 3.seconds.inWholeMilliseconds)
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                if (!outdated.getAndSet(true)) {
                    logger.warn("Could not load the servers from SimpleCloud, retrying: ${e.message}")
                }
            }
        }
    }

    private fun reconcile(servers: List<Server>, registration: RegistrationConfig, changedSince: Long) {
        recentEvents.values.removeIf { time -> time < changedSince }
        val changedServerIds = recentEvents.keys.toSet()
        val serverIds = servers.map { server -> server.serverId }.toSet()
        skippedServerIds.retainAll(serverIds)
        registrations.findAll()
            .filter { server -> server.serverId !in serverIds && server.serverId !in changedServerIds }
            .forEach { server -> unregister(server.serverId) }

        servers.filter { server -> server.serverId !in changedServerIds }.forEach { server ->
            if (server.state == ServerState.STOPPING || server.state == ServerState.CLEANUP) {
                cache.markStopping(server.serverId)
            } else {
                register(server, registration)
            }
        }
    }

    private suspend fun fetchServers(): List<Server> = api.server().getAllServers(
        ServerQuery.create()
            .filterByState(ServerState.AVAILABLE, ServerState.INGAME, ServerState.STOPPING, ServerState.CLEANUP)
            .filterByServerGroupType(GroupServerType.SERVER),
    ).await()

    private fun register(server: Server, registration: RegistrationConfig) {
        val registeredServer = convertToRegisteredServer(server, registration) ?: return
        val status = ServerStatus(
            stopping = false,
            playerCount = server.playerCount ?: 0,
            maxPlayers = getMaxPlayers(server),
        )

        cache.update(server.serverId, status)
        registerServer(registeredServer, registration)
    }

    private fun registerServer(server: RegisteredServer, registration: RegistrationConfig) {
        val ignoredNames = registration.ignoreServerGroupsAndPersistentServers
        if (ignoredNames.any { name -> name.equals(server.serverBaseName, ignoreCase = true) }) {
            unregister(server.serverId)
            return
        }

        val additionalNames = registration.additionalServers.map { additionalServer -> additionalServer.name }
        if (additionalNames.any { name -> name.equals(server.proxyName, ignoreCase = true) }) {
            warnSkipped(server.serverId, "the additional server ${server.proxyName} uses the same name")
            unregister(server.serverId)
            return
        }

        registrations.register(server)
    }

    private fun update(server: Server, registration: RegistrationConfig) {
        val previousServer = registrations.find(server.serverId) ?: return
        val registeredServer = convertToRegisteredServer(server, registration) ?: return
        if (registeredServer != previousServer) {
            registerServer(registeredServer, registration)
        }
    }

    private fun unregister(serverId: String) {
        cache.remove(serverId)
        registrations.unregister(serverId)
    }

    private fun convertToRegisteredServer(server: Server, registration: RegistrationConfig): RegisteredServer? {
        return try {
            val serverBase = server.serverBase
            if (serverBase.type != GroupServerType.SERVER || server.blueprint?.configurator == "standalone") return null

            val ip = server.ip
            val port = server.port
            if (ip == null || port == null) {
                warnSkipped(server.serverId, "ip or port is missing")
                return null
            }

            val properties = server.properties ?: emptyMap()
            val proxyName = RegisteredServerResolver.resolve(
                serverId = server.serverId,
                numericalId = server.numericalId,
                serverBaseName = serverBase.name,
                properties = properties,
                persistent = server.isFromPersistentServer,
                config = registration,
            )
            RegisteredServer(
                serverId = server.serverId,
                numericalId = server.numericalId,
                ip = ip,
                port = port,
                serverBaseName = serverBase.name,
                proxyName = proxyName,
                properties = properties,
                persistent = server.isFromPersistentServer,
            )
        } catch (e: IllegalStateException) {
            warnSkipped(server.serverId, e.message.orEmpty())
            null
        }
    }

    private fun isReadable(server: Server): Boolean = try {
        server.serverId.isNotEmpty() && server.state != ServerState.UNKNOWN_STATE
    } catch (_: IllegalStateException) {
        false
    }

    private fun getMaxPlayers(server: Server): Int = try {
        server.maxPlayers
    } catch (_: IllegalStateException) {
        0
    }

    private fun warnSkipped(serverId: String, reason: String) {
        if (!skippedServerIds.add(serverId)) return
        logger.warn("Skipping server $serverId: $reason")
    }
}
