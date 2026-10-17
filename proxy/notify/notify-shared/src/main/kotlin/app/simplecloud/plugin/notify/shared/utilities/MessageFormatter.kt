package app.simplecloud.plugin.notify.shared.utilities

import app.simplecloud.api.server.Server
import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.plugin.notify.shared.config.MessageConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class MessageFormatter(
    private val messages: ConfigurationFactory<MessageConfig>,
) {

    private val serverHoverPlaceholder = "notifications.hover.server"
    private val groupHoverPlaceholder = "notifications.hover.group"
    private val persistentServerHoverPlaceholder = "notifications.hover.persistent-server"

    private val zone: ZoneId = ZoneId.systemDefault()

    private val dateFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern(messages.get().format.date, Locale.getDefault()).withZone(zone)

    fun server(server: Server, message: String, vararg tagResolver: TagResolver): Component {
        return parse(
            replaceHover(message, serverHoverPlaceholder, messages.get().notifications.hover.server),
            Placeholder.unparsed("ip", server.ip ?: "N/A"),
            Placeholder.unparsed("port", server.port.toString()),
            Placeholder.unparsed("group", server.serverBase.name ?: "N/A"),
            Placeholder.unparsed("server", server.serverBase.name ?: "N/A"),
            Placeholder.unparsed("uuid", server.serverId),
            Placeholder.unparsed("id", formatNumericalId(server.numericalId)),
            Placeholder.unparsed("create_date", formatTimestamp(runCatching { server.createdAt }.getOrNull())),
            Placeholder.unparsed("time", formatTimestamp(Instant.now())),
            Placeholder.unparsed("updated", formatTimestamp(runCatching { server.updatedAt }.getOrNull())),
            Placeholder.unparsed("players", server.playerCount.toString()),
            Placeholder.unparsed("max", server.maxPlayers.toString()),
            *tagResolver
        )
    }

    fun group(message: String, groupName: String?): Component {
        return parse(
            replaceHover(message, groupHoverPlaceholder, messages.get().notifications.hover.group),
            Placeholder.unparsed("group", groupName ?: "N/A"),
            Placeholder.unparsed("time", formatTimestamp(Instant.now()))
        )
    }

    fun persistentServer(message: String, serverName: String?): Component {
        return parse(
            replaceHover(message, persistentServerHoverPlaceholder, messages.get().notifications.hover.persistentServer),
            Placeholder.unparsed("server", serverName ?: "N/A"),
            Placeholder.unparsed("time", formatTimestamp(Instant.now()))
        )
    }

    fun message(message: String, vararg tagResolver: TagResolver): Component {
        return parse(message, *tagResolver)
    }

    private fun parse(message: String, vararg tagResolver: TagResolver): Component {
        return messages.get().msg(message, *tagResolver)
    }

    private fun replaceHover(message: String, placeholder: String, hoverMessage: String): String {
        return message.replace(
            "<hover:show_text:'<$placeholder>'>",
            "<hover:show_text:'$hoverMessage'>"
        )
    }

    private fun formatNumericalId(id: Int): String {
        if (id == -1) return ""
        return " $id"
    }

    private fun formatTimestamp(value: Any?): String {
        val instant = when (value) {
            is Instant -> value
            is Date -> value.toInstant()
            is Long -> Instant.ofEpochMilli(value)
            is String -> runCatching { Instant.parse(value) }.getOrNull()
            else -> null
        } ?: return "N/A"

        return dateFormatter.format(instant)
    }

}