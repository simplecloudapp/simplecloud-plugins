package app.simplecloud.plugin.proxy.shared.network

import app.simplecloud.api.CloudApi
import app.simplecloud.api.event.Subscription
import app.simplecloud.api.group.Group
import app.simplecloud.api.persistentserver.PersistentServer
import app.simplecloud.api.server.Server
import app.simplecloud.api.server.ServerState
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.seconds

class NetworkCache(
    private val api: CloudApi,
    private val scope: CoroutineScope,
) {

    private val logger = LoggerFactory.getLogger(NetworkCache::class.java)
    private val servers = AtomicReference<Map<String, Server>>(emptyMap())
    private val groups = AtomicReference<Map<String, Group>>(emptyMap())
    private val persistentServers = AtomicReference<Map<String, PersistentServer>>(emptyMap())
    private val loaded = AtomicBoolean(false)
    private val subscriptions: MutableList<Subscription> = mutableListOf()

    fun isLoaded(): Boolean = loaded.get()

    fun getServers(): Collection<Server> = servers.get().values

    fun getServer(serverId: String): Server? = servers.get()[serverId]

    fun getServersOfGroup(groupName: String): List<Server> = getServers().filter { it.group?.name == groupName }

    fun findGroup(name: String): Group? = groups.get().values.find { it.name == name }

    fun findPersistentServer(name: String): PersistentServer? = persistentServers.get().values.find { it.name == name }

    fun start() {
        subscribeServers()
        subscribeGroups()
        subscribePersistentServers()

        scope.launch {
            while (isActive) {
                try {
                    reload()
                } catch (e: Exception) {
                    logger.error("Could not load the servers and groups of the network, keeping the last known state", e)
                }
                delay(if (isLoaded()) 30.seconds else 5.seconds)
            }
        }
    }

    fun stop() {
        subscriptions.forEach { subscription -> subscription.close() }
        subscriptions.clear()
    }

    private suspend fun reload() {
        val loadedGroups = api.group().allGroups.await().associateBy { it.serverGroupId }
        val loadedPersistentServers = api.persistentServer().allPersistentServers.await().associateBy { it.persistentServerId }
        val loadedServers = api.server().allServers.await()
            .filter { it.state != ServerState.STOPPED }
            .associateBy { it.serverId }

        groups.set(loadedGroups)
        persistentServers.set(loadedPersistentServers)
        servers.set(loadedServers)
        loaded.set(true)
    }

    private fun subscribeServers() {
        subscriptions.add(
            api.event().server().onStarted { event ->
                val server = event.server ?: return@onStarted
                putServer(server)
            },
        )
        subscriptions.add(
            api.event().server().onUpdated { event ->
                val server = event.server ?: return@onUpdated
                putServer(server)
            },
        )
        subscriptions.add(
            api.event().server().onStateChanged { event ->
                val server = event.server ?: return@onStateChanged
                if (event.newState == ServerState.STOPPED) {
                    removeServer(event.serverId)
                } else {
                    putServer(server)
                }
            },
        )
        subscriptions.add(api.event().server().onStopped { event -> removeServer(event.serverId) })
        subscriptions.add(api.event().server().onDeleted { event -> removeServer(event.serverId) })
    }

    private fun subscribeGroups() {
        subscriptions.add(
            api.event().group().onCreated { event ->
                val group = event.group ?: return@onCreated
                groups.updateAndGet { current -> current + (group.serverGroupId to group) }
            },
        )
        subscriptions.add(
            api.event().group().onUpdated { event ->
                val group = event.group ?: return@onUpdated
                groups.updateAndGet { current -> current + (group.serverGroupId to group) }
            },
        )
        subscriptions.add(
            api.event().group().onDeleted { event ->
                groups.updateAndGet { current -> current - event.serverGroupId }
            },
        )
    }

    private fun subscribePersistentServers() {
        subscriptions.add(api.event().persistentServer().onCreated { event -> refreshPersistentServer(event.persistentServerId) })
        subscriptions.add(api.event().persistentServer().onUpdated { event -> refreshPersistentServer(event.persistentServerId) })
        subscriptions.add(
            api.event().persistentServer().onDeleted { event ->
                persistentServers.updateAndGet { current -> current - event.persistentServerId }
            },
        )
    }

    private fun refreshPersistentServer(persistentServerId: String) {
        scope.launch {
            try {
                val persistentServer = api.persistentServer().getPersistentServerById(persistentServerId).await()
                persistentServers.updateAndGet { current -> current + (persistentServerId to persistentServer) }
            } catch (e: Exception) {
                logger.error("Could not load the persistent server '$persistentServerId'", e)
            }
        }
    }

    private fun putServer(server: Server) {
        if (server.state == ServerState.STOPPED) return
        servers.updateAndGet { current -> current + (server.serverId to server) }
    }

    private fun removeServer(serverId: String) {
        servers.updateAndGet { current -> current - serverId }
    }
}
