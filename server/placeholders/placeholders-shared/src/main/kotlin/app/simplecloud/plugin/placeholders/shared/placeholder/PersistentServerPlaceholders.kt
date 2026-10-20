package app.simplecloud.plugin.placeholders.shared.placeholder

import app.simplecloud.api.CloudApi
import app.simplecloud.api.persistentserver.PersistentServer
import app.simplecloud.api.runtime.SimpleCloudRuntime
import kotlinx.coroutines.future.await

object PersistentServerPlaceholders : PlaceholderType<PersistentServer> {

    override val prefix = "persistent_server"

    override val properties = setOf(
        "id",
        "name",
        "type",
        "host",
        "max_players",
        "min_memory",
        "max_memory",
        "player_count",
        "is_active",
    )

    override fun getValue(resource: PersistentServer, property: String): Any? = when (property) {
        "id" -> resource.persistentServerId
        "name" -> resource.name
        "type" -> resource.type
        "host" -> resource.serverhostId
        "max_players" -> resource.maxPlayers
        "min_memory" -> resource.minMemory
        "max_memory" -> resource.maxMemory
        "player_count" -> resource.playerCount
        "is_active" -> resource.isActive
        else -> null
    }

    override suspend fun loadCurrent(api: CloudApi): PersistentServer? =
        api.persistentServer().getPersistentServerById(SimpleCloudRuntime.serverId()).await()

    override suspend fun load(api: CloudApi, target: String): PersistentServer? =
        api.persistentServer().getPersistentServerByName(target).await()
}
