package app.simplecloud.plugin.placeholders.shared.placeholder

import app.simplecloud.api.CloudApi
import app.simplecloud.api.server.ServerState
import kotlinx.coroutines.future.await

object NetworkPlaceholders : PlaceholderType<NetworkPlaceholders.Data> {

    data class Data(val onlinePlayers: Int, val onlineServers: Int)

    override val prefix = "network"

    override val properties = setOf("online_players", "online_servers")

    override fun getValue(resource: Data, property: String): Any? = when (property) {
        "online_players" -> resource.onlinePlayers
        "online_servers" -> resource.onlineServers
        else -> null
    }

    override suspend fun loadCurrent(api: CloudApi): Data? {
        val onlinePlayers = api.player().onlinePlayerCount.await()
        val onlineServers = api.server().allServers.await().count {
            it.state == ServerState.AVAILABLE || it.state == ServerState.INGAME
        }
        return Data(onlinePlayers, onlineServers)
    }

    override suspend fun load(api: CloudApi, target: String): Data? = null
}
