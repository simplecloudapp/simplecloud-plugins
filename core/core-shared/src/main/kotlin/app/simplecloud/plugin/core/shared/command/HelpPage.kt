package app.simplecloud.plugin.core.shared.command

import app.simplecloud.plugin.core.shared.config.AbstractMessageConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/** Renders help lists: one row per subcommand, the description in the hover, a click types the command. */
object HelpPage {

    private val argumentStart = Regex(""" [<\[]""")

    /** One row per usage of [root], e.g. `group info <group>` for `/cloud`. */
    fun entries(config: AbstractMessageConfig, help: HelpMessages, root: String, usages: List<String>): List<Component> =
        usages.map { usage ->
            val split = argumentStart.find(usage)?.range?.first ?: usage.length
            val literal = usage.substring(0, split)
            val row = TagResolver.resolver(
                Placeholder.unparsed("command", literal),
                Placeholder.unparsed("arguments", usage.substring(split).trim()),
            )
            val hover = TagResolver.resolver(
                Placeholder.unparsed("description", help.descriptions[usage].orEmpty()),
                Placeholder.unparsed("usage", "$root $usage"),
            )

            config.msg(help.entry, row)
                .hoverEvent(HoverEvent.showText(config.msg(help.hover, hover)))
                .clickEvent(ClickEvent.suggestCommand("$root $literal "))
        }
}
