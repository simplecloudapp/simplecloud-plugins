package app.simplecloud.plugin.core.shared.placeholder.argument.group

import app.simplecloud.api.CloudApi
import app.simplecloud.api.group.Group
import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.core.shared.placeholder.argument.ArgumentsResolver
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue

class ServerCountArgumentsResolver(private val cloudApi: CloudApi, private val group: Group) : ArgumentsResolver {

    override fun getKey() = "server_count"

    override suspend fun resolve(arguments: ArgumentQueue): Tag? {
        val text = arguments.popOr("all").value()
        val serverState = ServerState.entries.firstOrNull { it.name.equals(text, true) }
        return Tag.preProcessParsed(findServerCount(this.group, serverState).toString())
    }

    private suspend fun findServerCount(group: Group, state: ServerState?): Int {
        val servers = this.cloudApi.server().getServersByGroup(group.name).await()
        return if (state != null) {
            servers.count { it.state == state }
        } else {
            servers.size
        }
    }
}
