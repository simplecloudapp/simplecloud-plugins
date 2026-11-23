package app.simplecloud.plugin.core.shared.config.message

import app.simplecloud.plugin.core.shared.adventure.miniMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

abstract class AbstractMessageConfig(
    palette: Map<String, String>,
    variables: Map<String, String>,
) {

    val tags: TagResolver = createTags(palette, variables)

    fun msg(
        message: String,
        vararg extra: TagResolver,
    ): Component = miniMessage.deserialize(message, TagResolver.resolver(*extra, tags))

    private fun createTags(palette: Map<String, String>, variables: Map<String, String>): TagResolver {
        val colors = MessagePalette.resolver(palette)
        val variableTags = variables.map { (key, value) ->
            TagResolver.resolver(key, Tag.selfClosingInserting(miniMessage.deserialize(value, colors)))
        }
        return TagResolver.resolver(colors, TagResolver.resolver(variableTags))
    }
}
