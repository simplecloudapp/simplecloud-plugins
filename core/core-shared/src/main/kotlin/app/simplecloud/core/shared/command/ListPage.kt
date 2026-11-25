package app.simplecloud.core.shared.command

import app.simplecloud.core.shared.config.message.AbstractMessageConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object ListPage {

    const val SIZE = 10

    fun count(entries: Int): Int = maxOf(1, (entries + SIZE - 1) / SIZE)

    fun <T> entries(entries: List<T>, page: Int): List<T> = entries.drop((page - 1) * SIZE).take(SIZE)

    fun footer(config: AbstractMessageConfig, messages: PageMessages, page: Int, pages: Int, command: String): Component =
        config.msg(
            messages.footer,
            Placeholder.unparsed("page", page.toString()),
            Placeholder.unparsed("pages", pages.toString()),
            button(config, "previous", messages.previous, command.replace("<page>", "${page - 1}"), page > 1),
            button(config, "next", messages.next, command.replace("<page>", "${page + 1}"), page < pages),
        )

    private fun button(config: AbstractMessageConfig, name: String, message: String, command: String, visible: Boolean): TagResolver {
        if (!visible) return Placeholder.component(name, Component.empty())
        return Placeholder.component(name, config.msg(message).clickEvent(ClickEvent.runCommand(command)))
    }
}
