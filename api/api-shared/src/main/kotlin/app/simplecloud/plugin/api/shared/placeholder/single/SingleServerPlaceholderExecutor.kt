package app.simplecloud.plugin.api.shared.placeholder.single

import app.simplecloud.api.CloudApi
import app.simplecloud.api.server.Server
import app.simplecloud.plugin.api.shared.placeholder.async.AsyncPlaceholder
import app.simplecloud.plugin.api.shared.pretty.StringPrettifier

/**
 * @author Niklas Nieberler
 */

class SingleServerPlaceholderExecutor : SinglePlaceholderExecutor<Server> {

    override fun getAsyncPlaceholders(cloudApi: CloudApi) = listOf<AsyncPlaceholder<Server>>(
        AsyncPlaceholder("id") { it.serverId },
        AsyncPlaceholder("numerical_id") { it.numericalId },
        AsyncPlaceholder("group_name") { it.serverBase.name },
        AsyncPlaceholder("group_pretty_name") {
            it.properties["pretty-name"] ?: StringPrettifier.prettify(it.serverBase.name)
        },
        AsyncPlaceholder("type") { it.serverBase.type },
        AsyncPlaceholder("state") { it.state },
        AsyncPlaceholder("ip") { it.ip },
        AsyncPlaceholder("port") { it.port },
        AsyncPlaceholder("online_players") { it.playerCount },
        AsyncPlaceholder("max_players") { it.maxPlayers },
        AsyncPlaceholder("min_memory") { it.minMemory },
        AsyncPlaceholder("max_memory") { it.maxMemory },
        AsyncPlaceholder("motd") { it.properties["motd"] ?: "A Minecraft server" }
    )

}