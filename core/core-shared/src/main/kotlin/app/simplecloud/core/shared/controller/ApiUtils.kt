package app.simplecloud.core.shared.controller

import app.simplecloud.api.CloudApi
import app.simplecloud.api.CloudApiException
import app.simplecloud.api.base.ServerBase
import app.simplecloud.api.group.Group
import app.simplecloud.api.group.GroupStartQueueEntry
import app.simplecloud.api.persistentserver.PersistentServer
import app.simplecloud.api.player.CloudPlayer
import app.simplecloud.api.player.CloudPlayer.ConnectResult
import app.simplecloud.api.player.ServerSelectionMode
import app.simplecloud.api.server.Server
import app.simplecloud.core.shared.command.CloudSuggestions
import kotlinx.coroutines.*
import kotlinx.coroutines.future.*
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeoutException

object ApiUtils {

    private val logger = LoggerFactory.getLogger(ApiUtils::class.java)
    private val statusCodeSuffix = Regex("""\s*\(\d+\)$""")
    private val apiAvailable = isApiAvailable()

    suspend fun findGroup(api: CloudApi, name: String): Group? =
        api.group().findGroupByName(name).await().orElse(null)
            ?: api.group().allGroups.await().firstOrNull { it.name.equals(name, true) }

    suspend fun findPersistentServer(api: CloudApi, name: String): PersistentServer? =
        api.persistentServer().findPersistentServerByName(name).await().orElse(null)
            ?: api.persistentServer().allPersistentServers.await().firstOrNull { it.name.equals(name, true) }

    suspend fun findServer(api: CloudApi, name: String): Server? {
        val persistentServer = findPersistentServer(api, name)
        if (persistentServer != null) return findRunningServer(api, persistentServer)
        val groupName = name.substringBeforeLast('-', "")
        val numericalId = name.substringAfterLast('-').toIntOrNull() ?: return null
        if (groupName.isBlank()) return null
        val group = findGroup(api, groupName) ?: return null
        return findServer(api, group, numericalId)
    }

    suspend fun findServer(api: CloudApi, group: Group, numericalId: Int): Server? =
        api.server().findServerByNumericalId(group.name, numericalId).await().orElse(null)

    suspend fun findRunningServer(api: CloudApi, persistentServer: PersistentServer): Server? =
        api.server().allServers.await().firstOrNull {
            it.persistentServerId == persistentServer.persistentServerId && !it.isStopped()
        }

    suspend fun findPlayer(api: CloudApi, name: String): CloudPlayer? =
        api.player().find(name).await().orElse(null)

    suspend fun getRunningServers(api: CloudApi, group: Group): List<Server> =
        api.server().getServersByGroup(group.name).await().filter { it.isRunning() }

    suspend fun findStartQueue(api: CloudApi, group: Group): GroupStartQueueEntry? =
        api.group().serverStartQueue.await().findByServerGroupId(group.serverGroupId)

    suspend fun getQueuedStarts(api: CloudApi, group: Group): Int = findStartQueue(api, group)?.queuedStarts() ?: 0

    suspend fun getPlayersOn(api: CloudApi, identifiers: Set<String>): List<CloudPlayer> =
        api.player().onlinePlayers.await().filter { player ->
            player.connectedServerName?.lowercase() in identifiers || player.connectedProxyName?.lowercase() in identifiers
        }

    suspend fun findTarget(api: CloudApi, targetType: String, name: String): Any? = when {
        targetType.equals(CloudSuggestions.TARGET_TYPE_GROUP, true) -> findGroup(api, name)
        targetType.equals(CloudSuggestions.TARGET_TYPE_PERSISTENT_SERVER, true) -> findPersistentServer(api, name)
        targetType.equals(CloudSuggestions.TARGET_TYPE_SERVER, true) -> findServer(api, name)
        else -> null
    }

    fun getTargetName(target: Any): String = when (target) {
        is ServerBase -> target.name
        is Server -> target.getDisplayName()
        else -> target.toString()
    }

    suspend fun getPlayersOnTarget(api: CloudApi, target: Any): List<CloudPlayer> = when (target) {
        is Group -> getPlayersOn(api, api.server().getServersByGroup(target.name).await().flatMap { it.getIdentifiers() }.toSet())
        is PersistentServer -> getPlayersOn(api, target.getIdentifiers())
        is Server -> getPlayersOn(api, target.getIdentifiers())
        else -> emptyList()
    }

    suspend fun connect(player: CloudPlayer, target: Any, mode: ServerSelectionMode?): ConnectResult = when (target) {
        is Group -> player.connectToGroup(target.name, mode ?: ServerSelectionMode.RANDOM).await()
        is PersistentServer -> player.connectToPersistentServer(target.persistentServerId).await()
        is Server -> player.connect(target.getDisplayName()).await()
        else -> ConnectResult.SERVER_NOT_FOUND
    }

    suspend fun connectAll(players: List<CloudPlayer>, target: Any, mode: ServerSelectionMode?): List<ConnectResult?> =
        coroutineScope {
            players.map { player -> async { runCatching { connect(player, target, mode) }.getOrNull() } }.awaitAll()
        }

    fun reason(throwable: Throwable): String {
        val causes = generateSequence(throwable) { it.cause }
        if (!apiAvailable) return nonApiReason(throwable, causes)

        val exception = causes.filterIsInstance<CloudApiException>().firstOrNull()

        return when {
            exception == null -> nonApiReason(throwable, causes)
            exception.reason == CloudApiException.Reason.UNAVAILABLE && exception.statusCode <= 0 -> "the controller is not reachable"
            exception.reason == CloudApiException.Reason.UNAVAILABLE -> "the controller is currently unavailable"
            else -> formatMessage(exception.message)
        }
    }

    private fun isApiAvailable(): Boolean = try {
        Class.forName("app.simplecloud.api.CloudApiException", false, ApiUtils::class.java.classLoader)
        true
    } catch (_: ClassNotFoundException) {
        false
    }

    private fun nonApiReason(throwable: Throwable, causes: Sequence<Throwable>): String {
        if (causes.any { it is TimeoutException }) return "the controller did not respond in time"
        if (causes.any { it is NoSuchMethodError || it is NoClassDefFoundError }) {
            logger.error("The installed simplecloud-api plugin is older than this plugin needs, update it", throwable)
            return "the installed simplecloud-api plugin is outdated"
        }
        logger.error("An unexpected error occurred while executing a command", throwable)
        return "an internal error occurred"
    }

    private fun formatMessage(error: String?): String {
        val message = error.orEmpty().replace(statusCodeSuffix, "").trim().trimEnd('.')
        if (message.isBlank()) return "an unknown error occurred"
        val isAcronym = message.length > 1 && message[1].isUpperCase()
        return if (isAcronym) message else message.replaceFirstChar { it.lowercase() }
    }
}
