package app.simplecloud.plugin.signs.shared

import app.simplecloud.api.CloudApi
import app.simplecloud.api.event.Subscription
import app.simplecloud.api.server.Server
import app.simplecloud.api.server.ServerQuery
import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.core.shared.adventure.miniMessage
import app.simplecloud.plugin.core.shared.config.ConfigWatcher
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.core.shared.config.DefaultConfigInstaller
import app.simplecloud.plugin.signs.shared.config.MessageConfig
import app.simplecloud.plugin.signs.shared.config.layout.LayoutConfig
import app.simplecloud.plugin.signs.shared.config.location.LocationsConfig
import app.simplecloud.plugin.signs.shared.config.location.SignLocation
import app.simplecloud.plugin.signs.shared.repository.LocationRepository
import app.simplecloud.plugin.signs.shared.repository.YamlDirectory
import app.simplecloud.plugin.signs.shared.rule.SignRule
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class SignManager(
    val api: CloudApi,
    private val dataDirectory: Path,
    private val renderer: SignRenderer,
    private val messages: ConfigurationFactory<MessageConfig>,
) {

    private val logger = LoggerFactory.getLogger(SignManager::class.java)

    val scope = CoroutineScope(
        CoroutineName("simplecloud-signs") +
            SupervisorJob() +
            Dispatchers.IO +
            CoroutineExceptionHandler { _, exception -> logger.error("An error occurred in the signs plugin", exception) },
    )

    private val locations = LocationRepository(dataDirectory.resolve("locations"))
    private val layouts = YamlDirectory(dataDirectory.resolve("layouts"), LayoutConfig::class.java)

    private val servers = ConcurrentHashMap<String, List<Server>>()
    private val failedTargets = ConcurrentHashMap.newKeySet<String>()
    private val assigned = ConcurrentHashMap<SignLocation, Server>()
    private val rendered = ConcurrentHashMap<SignLocation, RenderedSign>()
    private val lastRedraw = AtomicLong()
    private val refreshRequests = Channel<Unit>(Channel.CONFLATED)
    private val subscriptions = mutableListOf<Subscription>()

    fun start() {
        locations.load()
        layouts.load()
        logger.info("Loaded {} sign layouts", layouts.findAll().size)

        ConfigWatcher(dataDirectory) { reload() }.start(scope)
        subscribeToServerEvents()
        scope.launch {
            while (isActive) {
                try {
                    refreshServers()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.error("Failed to refresh the servers of the signs", e)
                }
                delay(MIN_REFRESH_GAP)
                withTimeoutOrNull(SERVER_REFRESH_INTERVAL) { refreshRequests.receive() }
            }
        }
    }

    fun reload() {
        messages.reload()
        locations.load()
        layouts.load()
    }

    fun tick() {
        try {
            updateSigns()
        } catch (e: Exception) {
            logger.error("Failed to update the signs", e)
        }
    }

    fun stop() {
        subscriptions.forEach { it.close() }
        scope.cancel()
    }

    fun configs(): List<LocationsConfig> = locations.findAll()

    fun isRegistered(location: SignLocation): Boolean = locations.isRegistered(location)

    fun serverAt(location: SignLocation): Server? = assigned[location]

    fun proxyName(server: Server): String =
        ServerNames.proxyName(server, findLayout(server)?.serverName ?: LayoutConfig().serverName)

    fun registerForGroup(group: String, location: SignLocation) {
        locations.addToGroup(group, location)
    }

    fun registerForPersistentServer(persistentServer: String, location: SignLocation) {
        locations.addToPersistentServer(persistentServer, location)
    }

    fun remove(location: SignLocation) {
        locations.remove(location)
        forget(location)
    }

    fun removeAll(identifier: String): Int {
        val removed = locations.removeAll(identifier)
        removed.forEach { forget(it) }
        return removed.size
    }

    private fun forget(location: SignLocation) {
        assigned.remove(location)
        rendered.remove(location)
        renderer.clear(location)
    }

    private fun subscribeToServerEvents() {
        try {
            val events = api.event().server()
            subscriptions += events.onStarted { onServerEvent() }
            subscriptions += events.onStopped { onServerEvent() }
            subscriptions += events.onStateChanged { onServerEvent() }
            subscriptions += events.onDeleted { onServerEvent() }
            subscriptions += events.onUpdated { onServerEvent() }
        } catch (e: Exception) {
            logger.warn("Failed to subscribe to server events, signs only update every second", e)
        }
    }

    private fun onServerEvent() {
        refreshRequests.trySend(Unit)
        scope.launch {
            delay(EVENT_RECHECK_DELAY)
            refreshRequests.trySend(Unit)
        }
    }

    private suspend fun refreshServers() {
        val configs = locations.findAll().distinctBy { it.identifier() }
        servers.keys.retainAll(configs.map { it.identifier() }.toSet())
        coroutineScope {
            configs.map { config -> async { refreshServers(config) } }.awaitAll()
        }
    }

    private suspend fun refreshServers(config: LocationsConfig) {
        val identifier = config.identifier()
        try {
            servers[identifier] = fetchServers(config)
            failedTargets.remove(identifier)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (failedTargets.add(identifier)) {
                logger.warn("Failed to fetch the servers of {}: {}", identifier, e.message)
            }
        }
    }

    private suspend fun fetchServers(config: LocationsConfig): List<Server> {
        val group = config.group
        if (!group.isNullOrEmpty()) return api.server().getServersByGroup(group).await()

        val name = config.persistentServer
        if (name.isNullOrEmpty()) return emptyList()
        val persistentServer = api.persistentServer().getPersistentServerByName(name).await()
        val query = ServerQuery.create().filterByPersistentServerId(persistentServer.persistentServerId)
        return api.server().getAllServers(query).await()
    }

    private fun updateSigns() {
        val now = System.currentTimeMillis()
        if (now - lastRedraw.get() >= REDRAW_INTERVAL) {
            lastRedraw.set(now)
            rendered.clear()
        }
        val configs = locations.findAll()
        val registered = configs.flatMap { it.locations }.toSet()
        assigned.keys.retainAll(registered)
        rendered.keys.retainAll(registered)
        configs.forEach { updateSigns(it, servers[it.identifier()].orEmpty()) }
    }

    private fun updateSigns(config: LocationsConfig, servers: List<Server>) {
        val liveServers = servers.filter { isLive(it) }
        val serversById = liveServers.associateBy { it.serverId }
        config.locations.forEach { location ->
            val current = assigned[location] ?: return@forEach
            val updated = serversById[current.serverId]
            if (updated == null) assigned.remove(location) else assigned[location] = updated
        }

        val taken = assigned.values.map { it.serverId }.toSet()
        val free = liveServers
            .filter { it.serverId !in taken }
            .sortedWith(SERVER_ORDER)
            .iterator()

        config.locations.forEach { location ->
            if (!assigned.containsKey(location) && free.hasNext()) {
                assigned[location] = free.next()
            }
            render(location, assigned[location], config.identifier())
        }
    }

    private fun isLive(server: Server): Boolean =
        server.state !in STOPPED_STATES && SignRule.entries.any { it.matches(server) }

    private fun render(location: SignLocation, server: Server?, target: String) {
        val layout = findLayout(server) ?: return
        if (layout.frames.isEmpty()) return

        val frameIndex = System.currentTimeMillis() / layout.frameUpdateInterval.coerceAtLeast(1) % layout.frames.size
        val frame = layout.frames[frameIndex.toInt()]
        val placeholders = SignPlaceholders.of(server, target)
        val sign = RenderedSign(
            frame.lines.take(MAX_LINES).map { miniMessage.deserialize(it, placeholders, messages.get().tags) },
            layout.behindBlock,
            layout.glowingText,
        )
        if (rendered.put(location, sign) != sign) {
            renderer.render(location, sign)
        }
    }

    private fun findLayout(server: Server?): LayoutConfig? {
        val serverName = ServerNames.displayName(server)
        return layouts.findAll()
            .sortedByDescending { it.priority }
            .firstOrNull { it.appliesTo(server) && it.ruleMatches(server) && it.nameMatches(serverName) }
    }

    companion object {
        private const val REDRAW_INTERVAL = 5000L
        private const val SERVER_REFRESH_INTERVAL = 1000L
        private const val MIN_REFRESH_GAP = 250L
        private const val EVENT_RECHECK_DELAY = 500L
        private const val MAX_LINES = 4

        private val STOPPED_STATES = setOf(ServerState.STOPPING, ServerState.CLEANUP, ServerState.STOPPED)

        private val DEFAULT_LAYOUTS = listOf("empty", "full", "maintenance", "offline", "online", "starting")

        private val SERVER_ORDER = Comparator<Server> { a, b ->
            when {
                a.isFromGroup && b.isFromGroup -> a.numericalId.compareTo(b.numericalId)
                a.isFromPersistentServer && b.isFromPersistentServer -> a.serverId.compareTo(b.serverId)
                a.isFromGroup -> -1
                else -> 1
            }
        }

        fun installDefaults(dataDirectory: Path) {
            if (Files.exists(dataDirectory.resolve("layouts"))) return
            DefaultConfigInstaller.installAll(
                dataDirectory,
                DEFAULT_LAYOUTS.map { "layouts/${it}_default.yml" },
                SignManager::class.java.classLoader,
            )
        }
    }
}
