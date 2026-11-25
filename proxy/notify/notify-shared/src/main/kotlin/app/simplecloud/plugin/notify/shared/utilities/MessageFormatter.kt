package app.simplecloud.plugin.notify.shared.utilities

import app.simplecloud.api.event.server.ServerStoppedEvent
import app.simplecloud.api.server.Server
import app.simplecloud.plugin.notify.shared.config.Hover
import app.simplecloud.plugin.notify.shared.config.MessageConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object MessageFormatter {

    fun server(config: MessageConfig, server: Server, message: String, vararg tagResolver: TagResolver): Component {
        val createdAt = runCatching { Instant.parse(server.createdAt) }.getOrNull()
        val updatedAt = runCatching { Instant.parse(server.updatedAt) }.getOrNull()
        return config.msg(
            replaceHovers(message, config.notifications.hover),
            Placeholder.unparsed("kind", formatKind(config, server)),
            Placeholder.unparsed("ip", server.ip ?: "N/A"),
            Placeholder.unparsed("port", server.port?.toString() ?: "N/A"),
            Placeholder.unparsed("group", server.serverBase.name),
            Placeholder.unparsed("server", formatServerName(server)),
            Placeholder.unparsed("uuid", server.serverId),
            Placeholder.unparsed("id", formatNumericalId(server)),
            Placeholder.unparsed("create_date", formatTime(config, createdAt)),
            Placeholder.unparsed("time", formatTime(config, Instant.now())),
            Placeholder.unparsed("updated", formatTime(config, updatedAt)),
            Placeholder.unparsed("players", (server.playerCount ?: 0).toString()),
            Placeholder.unparsed("max", server.maxPlayers?.toString() ?: "N/A"),
            *tagResolver,
        )
    }

    fun stoppedServer(config: MessageConfig, event: ServerStoppedEvent, message: String): Component = server(
        config,
        event.server,
        message,
        Placeholder.unparsed("state", if (event.isCrashed) "Crashed" else "Stopped"),
        Placeholder.unparsed("exit_code", event.exitCode?.toString() ?: "N/A"),
        Placeholder.unparsed("reason", event.reason ?: "N/A"),
    )

    fun group(config: MessageConfig, message: String, groupName: String): Component = config.msg(
        replaceHovers(message, config.notifications.hover),
        Placeholder.unparsed("group", groupName),
        Placeholder.unparsed("time", formatTime(config, Instant.now())),
    )

    fun persistentServer(config: MessageConfig, message: String, serverName: String): Component = config.msg(
        replaceHovers(message, config.notifications.hover),
        Placeholder.unparsed("server", serverName),
        Placeholder.unparsed("time", formatTime(config, Instant.now())),
    )

    private fun replaceHovers(message: String, hover: Hover): String = message
        .replace(hoverTag("server"), hoverText(hover.server))
        .replace(hoverTag("stopped"), hoverText(hover.stopped))
        .replace(hoverTag("group"), hoverText(hover.group))
        .replace(hoverTag("persistent-server"), hoverText(hover.persistentServer))

    private fun hoverTag(name: String): String = "<hover:show_text:'<notifications.hover.$name>'>"

    private fun hoverText(text: String): String = "<hover:show_text:'$text'>"

    private fun formatKind(config: MessageConfig, server: Server): String {
        val kind = config.notifications.kind
        if (server.isFromPersistentServer) return kind.persistentServer
        return kind.server
    }

    private fun formatServerName(server: Server): String {
        val name = server.serverBase.name
        if (server.isFromPersistentServer || server.numericalId == -1) return name
        return "$name-${server.numericalId}"
    }

    private fun formatNumericalId(server: Server): String {
        if (server.isFromPersistentServer || server.numericalId == -1) return ""
        return " ${server.numericalId}"
    }

    private fun formatTime(config: MessageConfig, instant: Instant?): String {
        if (instant == null) return "N/A"
        return DateTimeFormatter.ofPattern(config.format.date, Locale.getDefault())
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }
}
