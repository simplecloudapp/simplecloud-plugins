package app.simplecloud.plugin.placeholders.shared.placeholder

import app.simplecloud.api.CloudApi
import app.simplecloud.api.group.Group
import app.simplecloud.api.runtime.SimpleCloudRuntime
import app.simplecloud.api.server.ServerQuery
import app.simplecloud.api.server.ServerState
import kotlinx.coroutines.future.await

object GroupPlaceholders : PlaceholderType<GroupPlaceholders.Data> {

    data class Data(val group: Group, val playerCount: Int)

    override val prefix = "group"

    override val properties = setOf("name", "type", "min_memory", "max_memory", "max_players", "player_count")

    override fun getValue(resource: Data, property: String): Any? = when (property) {
        "name" -> resource.group.name
        "type" -> resource.group.type
        "min_memory" -> resource.group.minMemory
        "max_memory" -> resource.group.maxMemory
        "max_players" -> resource.group.maxPlayers
        "player_count" -> resource.playerCount
        else -> null
    }

    override suspend fun loadCurrent(api: CloudApi): Data? {
        val groupName = SimpleCloudRuntime.groupName()
        if (groupName.isBlank()) return null
        return load(api, groupName)
    }

    override suspend fun load(api: CloudApi, target: String): Data? {
        val group = api.group().findGroupByName(target).await().orElse(null) ?: return null
        val query = ServerQuery.create().filterByServerGroupName(group.name)
        val servers = api.server().getAllServers(query).await().filter { it.state != ServerState.STOPPED }
        return Data(group, servers.sumOf { it.playerCount ?: 0 })
    }
}
