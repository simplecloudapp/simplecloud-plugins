package app.simplecloud.plugin.core.shared.command

import app.simplecloud.plugin.core.shared.config.AbstractMessageConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object HelpEntry {

    private val argumentStart = Regex(""" [<\[]""")

    /**
     * A help line for [command]. The literal part fills <command>, the arguments fill <arguments>, and a click
     * types the literal part into the chat.
     */
    fun create(config: AbstractMessageConfig, message: String, hover: String, command: String): Component {
        val split = argumentStart.find(command)?.range?.first ?: command.length
        val literal = command.substring(0, split)
        val arguments = command.substring(split).trim()

        return config.msg(
            message,
            TagResolver.resolver(Placeholder.unparsed("command", literal), Placeholder.unparsed("arguments", arguments)),
        )
            .hoverEvent(HoverEvent.showText(config.msg(hover)))
            .clickEvent(ClickEvent.suggestCommand(if (arguments.isEmpty()) literal else "$literal "))
    }
}
