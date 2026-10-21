package app.simplecloud.plugin.proxy.shared.joinstate

import app.simplecloud.api.CloudApi
import app.simplecloud.api.event.Subscription
import app.simplecloud.api.runtime.SimpleCloudRuntime
import app.simplecloud.api.internal.integration.player.ServerNameRegistry
import app.simplecloud.api.server.Server
import app.simplecloud.api.server.ServerQuery
import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.proxy.shared.config.ProxyEssentialsConfig
import app.simplecloud.plugin.proxy.shared.network.NetworkCache
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicReference

class JoinStateService(
    private val api: CloudApi,
    private val cache: NetworkCache,
    private val config: ConfigurationFactory<ProxyEssentialsConfig>,
    private val scope: CoroutineScope,
) {

    private val logger = LoggerFactory.getLogger(JoinStateService::class.java)
    private val localState = AtomicReference<String?>()
    private val lastGroupState = AtomicReference<String?>()
    private val subscriptions: MutableList<Subscription> = mutableListOf()

    fun getLocalState(): String = localState.get() ?: config.get().initialState

    suspend fun start() {
        val server = api.server().getCurrentServer().await()
        localState.set(readState(server.properties) ?: applyDefaultState(server))

        subscriptions.add(
            api.event().server().onUpdated { event ->
                if (event.serverId != server.serverId) return@onUpdated
                scope.launch { updateLocalState(event.server) }
            },
        )

        val groupName = server.group?.name ?: return
        lastGroupState.set(getGroupState(groupName))
        subscriptions.add(
            api.event().group().onUpdated { event ->
                val group = event.group ?: return@onUpdated
                if (group.name != groupName) return@onUpdated
                scope.launch { followGroupState(server.serverId, groupName, getState(group.properties)) }
            },
        )
    }

    fun stop() {
        subscriptions.forEach { subscription -> subscription.close() }
        subscriptions.clear()
    }

    fun getState(properties: Map<String, Any>?): String = readState(properties) ?: config.get().initialState

    suspend fun getGroupState(groupName: String): String =
        getState(api.group().getGroupByName(groupName).await().properties)

    suspend fun hasGroupState(groupName: String, state: String): Boolean {
        if (getGroupState(groupName) != state) return false
        return getRunningServersOfGroup(groupName).all { readState(it.properties) == state }
    }

    suspend fun getServiceState(groupName: String, numericalId: Int): String {
        val service = api.server().getServerByNumericalId(groupName, numericalId).await()
        return readState(service.properties) ?: getGroupState(groupName)
    }

    suspend fun getPersistentServerState(name: String): String =
        getState(api.persistentServer().getPersistentServerByName(name).await().properties)

    suspend fun getTargetState(name: String): String {
        if (isGroup(name)) return getGroupState(name)
        return getPersistentServerState(name)
    }

    fun findCachedTargetState(name: String): String? {
        val group = cache.findGroup(name)
        if (group != null) return getState(group.properties)

        val persistentServer = cache.findPersistentServer(name) ?: return null
        return getState(persistentServer.properties)
    }

    fun getServerState(serverName: String): String? {
        if (SimpleCloudRuntime.serverId().isBlank()) return config.get().initialState
        if (!cache.isLoaded()) return null

        val server = cache.getServers().find { getServerName(it) == serverName }
        if (server != null) return resolveState(server)
        return findExternalServerState(serverName) ?: config.get().initialState
    }

    suspend fun setGroupState(groupName: String, state: String) {
        val group = api.group().getGroupByName(groupName).await()
        api.group().updateGroupProperty(group.serverGroupId, KEY, state).await()
        getRunningServersOfGroup(groupName).forEach { setServerState(it.serverId, state) }
    }

    suspend fun setServiceState(groupName: String, numericalId: Int, state: String) {
        val service = api.server().getServerByNumericalId(groupName, numericalId).await()
        setServerState(service.serverId, state)
    }

    suspend fun setPersistentServerState(name: String, state: String) {
        val persistentServer = api.persistentServer().getPersistentServerByName(name).await()
        api.persistentServer().updatePersistentServerProperty(persistentServer.persistentServerId, KEY, state).await()
        api.server().getAllServers(ServerQuery.create().filterByPersistentServerId(persistentServer.persistentServerId)).await()
            .filter { it.state != ServerState.STOPPED }
            .forEach { setServerState(it.serverId, state) }
    }

    suspend fun isGroup(name: String): Boolean = api.group().findGroupByName(name).await().isPresent

    private suspend fun getRunningServersOfGroup(groupName: String): List<Server> =
        api.server().getServersByGroup(groupName).await().filter { it.state != ServerState.STOPPED }

    private suspend fun setServerState(serverId: String, state: String) {
        api.server().updateServerProperty(serverId, KEY, state).await()
    }

    private suspend fun updateLocalState(server: Server) {
        val state = try {
            readState(server.properties) ?: applyDefaultState(server)
        } catch (e: Exception) {
            logger.error("Could not apply the default join state to this server", e)
            return
        }

        if (localState.getAndSet(state) != state) {
            logger.info("Join state changed to '$state'")
        }
    }

    private suspend fun applyDefaultState(server: Server): String {
        val groupName = server.group?.name
        val persistentServerName = server.persistentServer?.name
        val state = when {
            groupName != null -> getGroupState(groupName)
            persistentServerName != null -> getPersistentServerState(persistentServerName)
            else -> config.get().initialState
        }

        setServerState(server.serverId, state)
        return state
    }

    private suspend fun followGroupState(serverId: String, groupName: String, groupState: String) {
        val previousGroupState = lastGroupState.getAndSet(groupState)
        if (previousGroupState == null || previousGroupState == groupState) return
        if (getLocalState() != previousGroupState) return

        try {
            setServerState(serverId, groupState)
        } catch (e: Exception) {
            logger.error("Could not synchronize the join state with group '$groupName'", e)
        }
    }

    private fun resolveState(server: Server): String {
        val state = readState(server.properties)
        if (state != null) return state

        val groupName = server.group?.name
        if (groupName != null) return getState(cache.findGroup(groupName)?.properties)

        val persistentServerName = server.persistentServer?.name ?: return config.get().initialState
        return getState(cache.findPersistentServer(persistentServerName)?.properties)
    }

    private fun findExternalServerState(serverName: String): String? =
        config.get().externalServerJoinstates.entries.find { it.key.equals(serverName, ignoreCase = true) }?.value

    private fun readState(properties: Map<String, Any>?): String? {
        val state = properties?.get(KEY)?.toString()
        if (state.isNullOrBlank()) return null
        return state
    }

    private fun getServerName(server: Server): String {
        val registeredName = ServerNameRegistry.nameOf(server.serverId).orElse(null)
        if (registeredName != null) return registeredName

        val groupName = server.group?.name
        if (groupName != null) return "$groupName-${server.numericalId}"
        return server.persistentServer?.name ?: server.serverId
    }

    companion object {
        const val KEY = "joinstate"
    }
}
