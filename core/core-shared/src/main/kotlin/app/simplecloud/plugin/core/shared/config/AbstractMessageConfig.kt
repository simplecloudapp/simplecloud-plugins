package app.simplecloud.plugin.core.shared.config

import app.simplecloud.plugin.core.shared.adventure.miniMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/** Base class for a message configuration with variables. */
abstract class AbstractMessageConfig {

    abstract val variables: Map<String, String>

    private val resolver: TagResolver by lazy {
        TagResolver.resolver(
            variables.map { (key, value) ->
                TagResolver.resolver(key, Tag.selfClosingInserting(miniMessage.deserialize(value)))
            },
        )
    }

    /** Parses the [message] with the [variables] and the [extra] resolvers. */
    fun msg(
        message: String,
        vararg extra: TagResolver
    ): Component = miniMessage.deserialize(message, TagResolver.resolver(resolver, *extra))
}
