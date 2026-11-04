package app.simplecloud.plugin.core.shared.command

import app.simplecloud.plugin.core.shared.config.AbstractMessageConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object HelpEntry {

    /** A help line for [command] that types the command up to its first argument into the chat when clicked. */
    fun create(config: AbstractMessageConfig, message: String, hover: String, command: String): Component =
        config.msg(message, Placeholder.unparsed("command", command))
            .hoverEvent(HoverEvent.showText(config.msg(hover)))
            .clickEvent(ClickEvent.suggestCommand(command.substringBefore("<")))
}
