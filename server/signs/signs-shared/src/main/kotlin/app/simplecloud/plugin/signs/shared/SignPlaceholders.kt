package app.simplecloud.plugin.signs.shared

import app.simplecloud.api.server.Server
import app.simplecloud.plugin.core.shared.adventure.tags
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object SignPlaceholders {

    fun of(server: Server?, target: String): TagResolver {
        if (server == null) return offline(target)
        return TagResolver.resolver(
            tags(
                "target" to target,
                "server-name" to ServerNames.displayName(server),
                "group" to ServerNames.groupName(server),
                "numerical-id" to server.numericalId,
                "persistent-server" to ServerNames.persistentServerName(server),
                "server-id" to server.serverId,
                "host" to server.serverhostId,
                "ip" to server.ip,
                "port" to server.port,
                "min-memory" to server.minMemory,
                "max-memory" to server.maxMemory,
                "max-players" to server.maxPlayers,
                "player-count" to server.playerCount,
                "state" to server.state,
            ),
            property(server.properties.orEmpty()),
            SignShine.resolver,
        )
    }

    private fun offline(target: String): TagResolver = TagResolver.resolver(
        tags(
            "target" to target,
            "server-name" to "unknown",
            "numerical-id" to 0,
            "server-id" to "unknown",
            "host" to "unknown",
            "ip" to "unknown",
            "port" to 0,
            "min-memory" to 0,
            "max-memory" to 0,
            "max-players" to 0,
            "player-count" to 0,
            "state" to "unknown",
            "group" to "",
            "persistent-server" to "",
        ),
        property(emptyMap()),
        SignShine.resolver,
    )

    private fun property(properties: Map<String, Any?>): TagResolver = TagResolver.resolver("property") { arguments, _ ->
        val name = arguments.popOr("property expected").value()
        val default = arguments.peek()?.value().orEmpty()
        Tag.preProcessParsed(properties[name]?.toString() ?: default)
    }
}
