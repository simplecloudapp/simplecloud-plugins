package app.simplecloud.plugin.api.shared.placeholder.argument.group

import app.simplecloud.api.CloudApi
import app.simplecloud.api.group.Group
import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.api.shared.placeholder.argument.ArgumentsResolver
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue

class PlayerCountArgumentsResolver(
    private val cloudApi: CloudApi,
    private val group: Group,
) : ArgumentsResolver {

    override fun getKey() = "player_count"

    override suspend fun resolve(arguments: ArgumentQueue): Tag? {
        val text = arguments.popOr("all").value()
        val serverState = ServerState.entries.firstOrNull { it.name.equals(text, true) }
        return Tag.preProcessParsed(findPlayerCount(this.group, serverState).toString())
    }

    private suspend fun findPlayerCount(group: Group, state: ServerState?): Int {
        val servers = this.cloudApi.server().getServersByGroup(group.name).await()
        return if (state != null) {
            servers.filter { it.state == state }.sumOf { it.playerCount }
        } else {
            servers.sumOf { it.playerCount }
        }
    }

}
