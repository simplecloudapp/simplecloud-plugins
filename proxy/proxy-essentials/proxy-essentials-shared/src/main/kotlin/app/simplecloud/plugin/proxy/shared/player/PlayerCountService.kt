package app.simplecloud.plugin.proxy.shared.player

import app.simplecloud.api.CloudApi
import app.simplecloud.api.group.Group
import app.simplecloud.api.runtime.SimpleCloudRuntime
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.proxy.shared.config.PlayerCountConfig
import app.simplecloud.plugin.proxy.shared.config.ProxyEssentialsConfig
import app.simplecloud.plugin.proxy.shared.network.NetworkCache
import app.simplecloud.plugin.proxy.shared.platform.ProxyPlatform
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class PlayerCountService(
    private val api: CloudApi,
    private val cache: NetworkCache,
    private val platform: ProxyPlatform,
    private val config: ConfigurationFactory<ProxyEssentialsConfig>,
    private val scope: CoroutineScope,
) {

    private val logger = LoggerFactory.getLogger(PlayerCountService::class.java)
    private val otherOnlinePlayers = AtomicInteger()
    private val networkMaxPlayers = AtomicInteger()
    private val missingGroups: MutableSet<String> = ConcurrentHashMap.newKeySet()

    fun getOnlinePlayers(): Int = platform.getOnlinePlayers() + otherOnlinePlayers.get()

    fun getMaxPlayers(): Int {
        val maxPlayers = networkMaxPlayers.get()
        if (maxPlayers <= 0) return platform.getMaxPlayers()
        return maxPlayers
    }

    fun start() {
        scope.launch {
            update(config.get().playerCount)
            while (isActive) {
                val updateTime = config.get().playerCount.updateTime
                if (updateTime <= 0) {
                    delay(1.seconds)
                    continue
                }

                delay((updateTime * 50L).milliseconds)
                update(config.get().playerCount)
            }
        }
    }

    private suspend fun update(config: PlayerCountConfig) {
        if (!config.enabled) {
            otherOnlinePlayers.set(0)
            networkMaxPlayers.set(0)
            return
        }

        try {
            updateCounts(config)
        } catch (e: Exception) {
            logger.error("Could not count the players of the network, showing the last known count", e)
        }
    }

    private suspend fun updateCounts(config: PlayerCountConfig) {
        if (!cache.isLoaded()) return
        val server = cache.getServer(SimpleCloudRuntime.serverId()) ?: return
        val groupNames = (listOfNotNull(server.group?.name) + config.additionalGroups).distinct()
        val groups = groupNames.mapNotNull { findGroup(it) }
        val groupServers = groups.flatMap { cache.getServersOfGroup(it.name) }
        val persistentServers = cache.getServers().filter {
            it.persistentServer?.name in
                config.additionalPersistentServers
        }

        val ownMaxPlayers = if (server.group == null) server.maxPlayers ?: 0 else 0
        val otherServers = (groupServers + persistentServers).filter { it.serverId != server.serverId }

        val otherPlayers = if (config.network) {
            (api.player().onlinePlayerCount.await() - platform.getOnlinePlayers()).coerceAtLeast(0)
        } else {
            otherServers.sumOf { it.playerCount ?: 0 }
        }
        otherOnlinePlayers.set(otherPlayers)
        networkMaxPlayers.set(
            ownMaxPlayers + groups.sumOf {
                it.maxPlayers ?: 0
            } + persistentServers.sumOf { it.maxPlayers ?: 0 },
        )
    }

    private fun findGroup(name: String): Group? {
        val group = cache.findGroup(name)
        if (group != null) {
            missingGroups.remove(name)
            return group
        }

        if (missingGroups.add(name)) {
            logger.warn("The group '$name' does not exist, so its players are not counted")
        }
        return null
    }
}
