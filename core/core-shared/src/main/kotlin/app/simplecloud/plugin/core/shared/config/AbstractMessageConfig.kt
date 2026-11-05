package app.simplecloud.plugin.core.shared.config

import app.simplecloud.plugin.core.shared.adventure.miniMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/** Base class for a message configuration with a color palette and variables. */
abstract class AbstractMessageConfig {

    abstract val palette: Map<String, String>

    abstract val variables: Map<String, String>

    /** Resolves the palette's color tags and the variables. */
    val tags: TagResolver by lazy {
        val colors = MessagePalette.resolver(palette)
        TagResolver.resolver(
            colors,
            TagResolver.resolver(
                variables.map { (key, value) ->
                    TagResolver.resolver(key, Tag.selfClosingInserting(miniMessage.deserialize(value, colors)))
                },
            ),
        )
    }

    /** Parses the [message] with the [extra] resolvers and the [tags]. Extra resolvers win over tags with the same name. */
    fun msg(
        message: String,
        vararg extra: TagResolver,
    ): Component = miniMessage.deserialize(message, TagResolver.resolver(*extra, tags))
}
