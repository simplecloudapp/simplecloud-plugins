package app.simplecloud.plugin.api.shared.placeholder.single

import app.simplecloud.api.CloudApi
import app.simplecloud.api.persistentserver.PersistentServer
import app.simplecloud.plugin.api.shared.placeholder.async.AsyncPlaceholder
import app.simplecloud.plugin.api.shared.pretty.StringPrettifier

/**
 * @author Niklas Nieberler
 */

class SinglePersistentServerPlaceholderExecutor : SinglePlaceholderExecutor<PersistentServer> {

    override fun getAsyncPlaceholders(cloudApi: CloudApi) = listOf<AsyncPlaceholder<PersistentServer>>(
        AsyncPlaceholder("id") { it.persistentServerId },
        AsyncPlaceholder("name") { it.name },
        AsyncPlaceholder("pretty_name") {
            it.properties["pretty-name"] ?: StringPrettifier.prettify(it.name)
        },
        AsyncPlaceholder("type") { it.type },
        AsyncPlaceholder("online_players") { it.playerCount },
        AsyncPlaceholder("max_players") { it.maxPlayers },
        AsyncPlaceholder("min_memory") { it.minMemory },
        AsyncPlaceholder("max_memory") { it.maxMemory },
        AsyncPlaceholder("motd") { it.properties["motd"] ?: "A Minecraft server" }
    )

}