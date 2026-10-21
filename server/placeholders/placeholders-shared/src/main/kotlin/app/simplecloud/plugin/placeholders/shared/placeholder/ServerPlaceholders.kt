package app.simplecloud.plugin.placeholders.shared.placeholder

import app.simplecloud.api.CloudApi
import app.simplecloud.api.server.Server
import kotlinx.coroutines.future.await

object ServerPlaceholders : PlaceholderType<Server> {

    private const val ID_PREFIX = "id:"

    override val prefix = "server"

    override val properties = setOf(
        "id",
        "name",
        "group",
        "type",
        "host",
        "numerical_id",
        "ip",
        "port",
        "max_players",
        "min_memory",
        "max_memory",
        "player_count",
        "state",
    )

    override fun getValue(resource: Server, property: String): Any? = when (property) {
        "id" -> resource.serverId
        "name" -> serverName(resource)
        "group" -> resource.serverBase.name
        "type" -> resource.serverBase.type
        "host" -> resource.serverhostId
        "numerical_id" -> resource.numericalId
        "ip" -> resource.ip
        "port" -> resource.port
        "max_players" -> resource.maxPlayers
        "min_memory" -> resource.minMemory
        "max_memory" -> resource.maxMemory
        "player_count" -> resource.playerCount
        "state" -> resource.state
        else -> null
    }

    override suspend fun loadCurrent(api: CloudApi): Server? = api.server().currentServer.await()

    private fun serverName(server: Server): String {
        val group = server.group
        if (group != null) return "${group.name}-${server.numericalId}"
        return server.persistentServer?.name ?: server.serverId
    }

    override suspend fun load(api: CloudApi, target: String): Server? =
        if (target.startsWith(ID_PREFIX)) loadById(api, target.removePrefix(ID_PREFIX)) else loadByNumericalId(api, target)

    private suspend fun loadById(api: CloudApi, serverId: String): Server? {
        if (serverId.isBlank()) return null
        return api.server().findServerById(serverId).await().orElse(null)
    }

    private suspend fun loadByNumericalId(api: CloudApi, target: String): Server? {
        val separator = target.lastIndexOf(':')
        if (separator <= 0) return null

        val groupName = target.substring(0, separator)
        val numericalId = target.substring(separator + 1).toIntOrNull() ?: return null
        return api.server().findServerByNumericalId(groupName, numericalId).await().orElse(null)
    }
}
