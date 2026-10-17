package app.simplecloud.plugin.api.shared.placeholder.single

import app.simplecloud.api.CloudApi
import app.simplecloud.api.group.Group
import app.simplecloud.plugin.api.shared.placeholder.async.AsyncPlaceholder
import kotlinx.coroutines.future.await

class SingleGroupPlaceholderExecutor : SinglePlaceholderExecutor<Group> {

    override fun getAsyncPlaceholders(cloudApi: CloudApi) = listOf<AsyncPlaceholder<Group>>(
        AsyncPlaceholder("name") { it.name },
        AsyncPlaceholder("type") { it.type },
        AsyncPlaceholder("max_players") { it.maxPlayers },
        AsyncPlaceholder("min_memory") { it.minMemory },
        AsyncPlaceholder("max_memory") { it.maxMemory },
        AsyncPlaceholder("online_players") { getOnlinePlayersByGroup(cloudApi, it) },
    )

    private suspend fun getOnlinePlayersByGroup(cloudApi: CloudApi, group: Group): Int {
        return cloudApi.server().getServersByGroup(group.name).await()
            .sumOf { it.playerCount }
    }

}
