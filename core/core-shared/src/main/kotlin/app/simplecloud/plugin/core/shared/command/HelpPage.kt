package app.simplecloud.plugin.core.shared.command

import app.simplecloud.plugin.core.shared.config.AbstractMessageConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * Renders help lists as aligned rows: the subcommand, its arguments, and a description in one column. Rows too wide
 * for the chat keep their description in the hover only.
 */
object HelpPage {

    private const val CHAT_WIDTH = 320
    private const val INDENT = 8
    private const val GAP = 12

    private val argumentStart = Regex(""" [<\[]""")

    /** One row per usage of [root], e.g. `group info <group>` for `/cloud`. */
    fun entries(config: AbstractMessageConfig, help: HelpMessages, root: String, usages: List<String>): List<Component> {
        val rows = usages.map { usage -> Row.of(usage, help.descriptions[usage].orEmpty()) }
        val column = rows.filter { fits(it, it.width) }.maxOfOrNull { it.width } ?: 0

        return rows.map { row -> entry(config, help, root, row, column) }
    }

    private fun entry(config: AbstractMessageConfig, help: HelpMessages, root: String, row: Row, column: Int): Component {
        val inline = row.width <= column && fits(row, column)
        val resolver = TagResolver.resolver(
            Placeholder.unparsed("command", row.literal),
            Placeholder.unparsed("arguments", row.arguments),
            Placeholder.unparsed("padding", if (inline) ChatWidth.spaces(column - row.width + GAP) else ""),
            Placeholder.unparsed("description", if (inline) row.description else ""),
        )
        val hover = TagResolver.resolver(
            Placeholder.unparsed("description", row.description),
            Placeholder.unparsed("usage", "$root ${row.usage}"),
        )

        return config.msg(help.entry, resolver)
            .hoverEvent(HoverEvent.showText(config.msg(help.hover, hover)))
            .clickEvent(ClickEvent.suggestCommand("$root ${row.literal} "))
    }

    private fun fits(row: Row, column: Int): Boolean =
        row.description.isNotEmpty() && INDENT + column + GAP + ChatWidth.of(row.description) <= CHAT_WIDTH

    private data class Row(val usage: String, val literal: String, val arguments: String, val description: String) {

        val width: Int = ChatWidth.of("$literal $arguments")

        companion object {
            fun of(usage: String, description: String): Row {
                val split = argumentStart.find(usage)?.range?.first ?: usage.length
                return Row(usage, usage.substring(0, split), usage.substring(split).trim(), description)
            }
        }
    }
}
