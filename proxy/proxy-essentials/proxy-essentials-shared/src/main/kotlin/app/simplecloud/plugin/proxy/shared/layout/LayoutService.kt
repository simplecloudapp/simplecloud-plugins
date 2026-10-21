package app.simplecloud.plugin.proxy.shared.layout

import app.simplecloud.api.CloudApi
import app.simplecloud.api.event.Subscription
import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.proxy.shared.config.LayoutConfig
import app.simplecloud.plugin.proxy.shared.config.MotdEntry
import app.simplecloud.plugin.proxy.shared.config.MotdUpdateType
import app.simplecloud.plugin.proxy.shared.config.ProxyEssentialsConfig
import app.simplecloud.plugin.proxy.shared.joinstate.JoinStateService
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

class LayoutService(
    private val api: CloudApi,
    private val repository: LayoutRepository,
    private val service: JoinStateService,
    private val config: ConfigurationFactory<ProxyEssentialsConfig>,
) {

    private val logger = LoggerFactory.getLogger(LayoutService::class.java)
    private val localLayout = AtomicReference<String?>()
    private val pingCounters = ConcurrentHashMap<String, AtomicInteger>()
    private val subscriptions: MutableList<Subscription> = mutableListOf()

    suspend fun start() {
        val server = api.server().getCurrentServer().await()
        val groupName = server.group?.name
        val groupLayout = if (groupName == null) null else getGroupLayout(groupName)
        localLayout.set(readLayout(server.properties) ?: groupLayout)

        subscriptions.add(
            api.event().server().onUpdated { event ->
                if (event.serverId != server.serverId) return@onUpdated

                val layout = readLayout(event.server.properties)
                if (layout != null && localLayout.getAndSet(layout) != layout) {
                    logger.info("Layout changed to '$layout'")
                }
            },
        )
    }

    fun stop() {
        subscriptions.forEach { subscription -> subscription.close() }
        subscriptions.clear()
    }

    fun getLayout(name: String): LayoutConfig = repository.find(name) ?: LayoutConfig()

    fun getLayoutName(domain: String?): String {
        val config = config.get()
        val domainLayout = if (domain == null) null else findDomainLayout(domain)
        val forcedLayout = config.joinstates.find { it.name == service.getLocalState() }?.forcedMotdLayout
        val candidates = listOf(domainLayout, forcedLayout, localLayout.get(), config.initialLayout)

        return candidates.filterNotNull().firstOrNull { repository.find(it) != null } ?: config.initialLayout
    }

    fun getMotdEntry(layoutName: String, layout: LayoutConfig): MotdEntry? {
        val motd = layout.motd
        if (motd.layouts.isEmpty()) return null

        val updateTime =
            motd.updateTime ?: return motd.layouts[getPingIndex(layoutName, motd.updateType, motd.layouts.size)]
        if (updateTime <= 0) return motd.layouts.first()

        val timeSlot = System.currentTimeMillis() / (updateTime * 50L)
        val index = when (motd.updateType) {
            MotdUpdateType.RANDOM -> Random(timeSlot).nextInt(motd.layouts.size)
            MotdUpdateType.QUEUE -> timeSlot.mod(motd.layouts.size)
        }
        return motd.layouts[index]
    }

    suspend fun getGroupLayout(groupName: String): String? =
        readLayout(api.group().getGroupByName(groupName).await().properties)

    suspend fun setGroupLayout(groupName: String, layout: String) {
        val group = api.group().getGroupByName(groupName).await()
        api.group().updateGroupProperty(group.serverGroupId, KEY, layout).await()
        api.server().getServersByGroup(groupName).await().filter { it.state != ServerState.STOPPED }.forEach {
            api.server().updateServerProperty(it.serverId, KEY, layout).await()
        }
    }

    private fun findDomainLayout(domain: String): String? {
        val config = config.get()
        val route = config.domains.find { it.domain.equals(domain, ignoreCase = true) } ?: return null
        val state = service.findCachedTargetState(route.target) ?: return null
        val candidates = listOf(
            route.rules.find { it.state == state }?.layout,
            config.joinstates.find { it.name == state }?.forcedMotdLayout,
            config.initialLayout,
        )

        return candidates.filterNotNull().firstOrNull { repository.find(it) != null }
    }

    private fun getPingIndex(layoutName: String, updateType: MotdUpdateType, size: Int): Int = when (updateType) {
        MotdUpdateType.RANDOM -> Random.nextInt(size)

        MotdUpdateType.QUEUE -> pingCounters.computeIfAbsent(layoutName) {
            AtomicInteger()
        }.getAndIncrement().mod(size)
    }

    private fun readLayout(properties: Map<String, Any>?): String? {
        val layout = properties?.get(KEY)?.toString()
        if (layout.isNullOrBlank()) return null
        return layout
    }

    companion object {
        const val KEY = "motd-layout"
    }
}
