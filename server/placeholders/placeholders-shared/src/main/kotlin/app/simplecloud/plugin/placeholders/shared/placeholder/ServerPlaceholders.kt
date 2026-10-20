package app.simplecloud.plugin.placeholders.shared.placeholder

import app.simplecloud.api.CloudApi
import app.simplecloud.api.runtime.SimpleCloudRuntime
import app.simplecloud.api.server.Server
import kotlinx.coroutines.future.await

object ServerPlaceholders : PlaceholderType<Server> {

    private const val ID_PREFIX = "id:"

    override val prefix = "server"

    override val properties = setOf(
        "id",
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

    override suspend fun loadCurrent(api: CloudApi): Server? = api.server().getServerById(SimpleCloudRuntime.serverId()).await()

    override suspend fun load(api: CloudApi, target: String): Server? =
        if (target.startsWith(ID_PREFIX)) loadById(api, target.removePrefix(ID_PREFIX)) else loadByNumericalId(api, target)

    private suspend fun loadById(api: CloudApi, serverId: String): Server? {
        if (serverId.isBlank()) return null
        return api.server().getServerById(serverId).await()
    }

    private suspend fun loadByNumericalId(api: CloudApi, target: String): Server? {
        val separator = target.lastIndexOf(':')
        if (separator <= 0) return null

        val groupName = target.substring(0, separator)
        val numericalId = target.substring(separator + 1).toIntOrNull() ?: return null
        return api.server().getServerByNumericalId(groupName, numericalId).await()
    }
}
