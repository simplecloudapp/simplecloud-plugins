package app.simplecloud.plugin.api.shared.command

import app.simplecloud.api.CloudApi
import app.simplecloud.api.group.GroupServerType
import app.simplecloud.api.server.Server
import app.simplecloud.api.server.ServerState
import kotlinx.coroutines.future.await
import org.incendo.cloud.kotlin.coroutines.SuspendingSuggestionProvider
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider

/**
 * Commonly used suggestion providers for commands.
 */
object CloudSuggestions {

    const val TARGET_TYPE_GROUP = "group"
    const val TARGET_TYPE_PERSISTENT_SERVER = "ps"

    /**
     * Suggests the names of all groups.
     */
    fun <C : Any> groups(api: CloudApi): SuggestionProvider<C> =
        SuspendingSuggestionProvider<C> { _, _ ->
            try {
                api.group().allGroups.await().map { Suggestion.suggestion(it.name) }
            } catch (_: Exception) {
                emptyList()
            }
        }.asSuggestionProvider()

    /**
     * Suggests the names of all groups of the given [type].
     */
    fun <C : Any> groups(api: CloudApi, type: GroupServerType): SuggestionProvider<C> =
        SuspendingSuggestionProvider<C> { _, _ ->
            try {
                api.group().allGroups.await()
                    .filter { it.type == type }
                    .map { Suggestion.suggestion(it.name) }
            } catch (_: Exception) {
                emptyList()
            }
        }.asSuggestionProvider()

    /**
     * Suggests the names of all proxy groups.
     */
    fun <C : Any> proxyGroups(api: CloudApi): SuggestionProvider<C> =
        groups(api, GroupServerType.PROXY)

    /**
     * Suggests the names of all server groups.
     */
    fun <C : Any> serverGroups(api: CloudApi): SuggestionProvider<C> =
        groups(api, GroupServerType.SERVER)

    /**
     * Suggests the numerical ids of the servers of the group stored in the argument [groupArgument].
     */
    fun <C : Any> serverIds(api: CloudApi, groupArgument: String = "group"): SuggestionProvider<C> =
        SuspendingSuggestionProvider<C> { context, _ ->
            try {
                val group = context.get<String>(groupArgument)
                api.server().getServersByGroup(group).await()
                    .sortedBy { it.numericalId }
                    .map { Suggestion.suggestion(it.numericalId.toString()) }
            } catch (_: Exception) {
                emptyList()
            }
        }.asSuggestionProvider()

    /**
     * Suggests the names of all running servers.
     */
    fun <C : Any> servers(api: CloudApi): SuggestionProvider<C> =
        SuspendingSuggestionProvider<C> { _, _ ->
            try {
                api.server().allServers.await()
                    .mapNotNull { serverName(it) }
                    .map { Suggestion.suggestion(it) }
            } catch (_: Exception) {
                emptyList()
            }
        }.asSuggestionProvider()

    /**
     * Suggests the names of all running servers in one of the given [states].
     */
    fun <C : Any> servers(api: CloudApi, vararg states: ServerState): SuggestionProvider<C> {
        val stateSet = states.toSet()
        return SuspendingSuggestionProvider<C> { _, _ ->
            try {
                api.server().allServers.await()
                    .filter { it.state in stateSet }
                    .mapNotNull { serverName(it) }
                    .map { Suggestion.suggestion(it) }
            } catch (_: Exception) {
                emptyList()
            }
        }.asSuggestionProvider()
    }

    /**
     * Suggests the names of all running servers that players can join.
     */
    fun <C : Any> availableServers(api: CloudApi): SuggestionProvider<C> =
        servers(api, ServerState.AVAILABLE, ServerState.INGAME)

    /**
     * Suggests the names of all persistent servers.
     */
    fun <C : Any> persistentServers(api: CloudApi): SuggestionProvider<C> =
        SuspendingSuggestionProvider<C> { _, _ ->
            try {
                api.persistentServer().allPersistentServers.await().map { Suggestion.suggestion(it.name) }
            } catch (_: Exception) {
                emptyList()
            }
        }.asSuggestionProvider()

    /**
     * Suggests the names of all online players.
     */
    fun <C : Any> onlinePlayers(api: CloudApi): SuggestionProvider<C> =
        SuspendingSuggestionProvider<C> { _, _ ->
            try {
                api.player().onlinePlayers.await().map { Suggestion.suggestion(it.name) }
            } catch (_: Exception) {
                emptyList()
            }
        }.asSuggestionProvider()

    /**
     * Suggests the names of all online players except the command sender.
     */
    fun <C : CloudSender> otherOnlinePlayers(api: CloudApi): SuggestionProvider<C> =
        SuspendingSuggestionProvider<C> { context, _ ->
            try {
                val uniqueId = context.sender().getUniqueId()
                api.player().onlinePlayers.await()
                    .filter { it.uniqueId != uniqueId }
                    .map { Suggestion.suggestion(it.name) }
            } catch (_: Exception) {
                emptyList()
            }
        }.asSuggestionProvider()

    /**
     * Suggests the names of all players connected to the server stored in the argument [serverArgument].
     */
    //fun <C : Any> playersOnServer(api: CloudApi, serverArgument: String = "server"): SuggestionProvider<C> =
    //    SuspendingSuggestionProvider<C> { context, _ ->
    //        try {
    //            val server = context.get<String>(serverArgument)
    //            api.player().getOnlinePlayers(server).await().map { Suggestion.suggestion(it.name) }
    //        } catch (_: Exception) {
    //            emptyList()
    //        }
    //    }.asSuggestionProvider()

    /**
     * Suggests the names of all players connected to the proxy stored in the argument [proxyArgument].
     */
    fun <C : Any> playersOnProxy(api: CloudApi, proxyArgument: String = "proxy"): SuggestionProvider<C> =
        SuspendingSuggestionProvider<C> { context, _ ->
            try {
                val proxy = context.get<String>(proxyArgument)
                api.player().onlinePlayers.await()
                    .filter { it.connectedProxyName.equals(proxy, true) }
                    .map { Suggestion.suggestion(it.name) }
            } catch (_: Exception) {
                emptyList()
            }
        }.asSuggestionProvider()

    /**
     * Suggests the target types [TARGET_TYPE_GROUP] and [TARGET_TYPE_PERSISTENT_SERVER].
     */
    fun <C : Any> targetTypes(): SuggestionProvider<C> =
        of(TARGET_TYPE_GROUP, TARGET_TYPE_PERSISTENT_SERVER)

    /**
     * Suggests group or persistent server names, depending on the target type stored in the argument [targetTypeArgument].
     */
    fun <C : Any> targets(api: CloudApi, targetTypeArgument: String = "targetType"): SuggestionProvider<C> =
        SuspendingSuggestionProvider<C> { context, _ ->
            try {
                val targetType = context.get<String>(targetTypeArgument)
                when {
                    targetType.equals(TARGET_TYPE_GROUP, true) -> api.group().allGroups.await().map { Suggestion.suggestion(it.name) }
                    targetType.equals(TARGET_TYPE_PERSISTENT_SERVER, true) -> api.persistentServer().allPersistentServers.await().map { Suggestion.suggestion(it.name) }
                    else -> emptyList()
                }
            } catch (_: Exception) {
                emptyList()
            }
        }.asSuggestionProvider()

    /**
     * Suggests all known [ServerState]s.
     */
    fun <C : Any> serverStates(): SuggestionProvider<C> =
        of(ServerState.entries.filter { it != ServerState.UNKNOWN_STATE }.map { it.name.lowercase() })

    //fun <C : Any> selectionModes(): SuggestionProvider<C> =
    //    of(ServerSelectionMode.entries.map { it.name.lowercase() })

    /**
     * Suggests all known [GroupServerType]s.
     */
    fun <C : Any> serverTypes(): SuggestionProvider<C> =
        of(GroupServerType.entries.filter { it != GroupServerType.UNKNOWN_SERVER }.map { it.name.lowercase() })

    /**
     * Suggests the given fixed [values].
     */
    fun <C : Any> of(vararg values: String): SuggestionProvider<C> =
        of(values.toList())

    /**
     * Suggests the given fixed [values].
     */
    fun <C : Any> of(values: Collection<String>): SuggestionProvider<C> {
        val suggestions = values.map { Suggestion.suggestion(it) }
        return SuggestionProvider.blocking { _, _ -> suggestions }
    }

    private fun serverName(server: Server): String? {
        val persistentServer = server.persistentServer
        if (persistentServer != null) return persistentServer.name
        val group = server.group ?: return null
        return "${group.name}-${server.numericalId}"
    }

}
