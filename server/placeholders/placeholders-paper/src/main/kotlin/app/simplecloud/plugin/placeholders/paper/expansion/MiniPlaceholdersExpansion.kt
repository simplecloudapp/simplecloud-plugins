package app.simplecloud.plugin.placeholders.paper.expansion

import app.simplecloud.plugin.placeholders.shared.BuildConstants
import app.simplecloud.plugin.placeholders.shared.placeholder.PlaceholderType
import app.simplecloud.plugin.placeholders.shared.query.PlaceholderQuery
import app.simplecloud.plugin.placeholders.shared.resolve.PlaceholderResolver
import io.github.miniplaceholders.api.Expansion
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue

class MiniPlaceholdersExpansion(
    private val resolver: PlaceholderResolver,
    types: List<PlaceholderType<*>>,
) {

    private val expansion: Expansion

    init {
        val builder = Expansion.builder("simplecloud").author("SimpleCloud").version(BuildConstants.VERSION)
        types.forEach { type ->
            type.properties.forEach { property ->
                builder.globalPlaceholder("${type.prefix}_$property") { queue, _ -> resolve(type, property, queue) }
            }
        }
        expansion = builder.build()
    }

    fun register() = expansion.register()

    fun unregister() {
        if (expansion.registered()) expansion.unregister()
    }

    private fun resolve(type: PlaceholderType<*>, property: String, queue: ArgumentQueue): Tag {
        val arguments = mutableListOf<String>()
        while (queue.hasNext()) arguments += queue.pop().value()

        val target = arguments.joinToString(":").ifEmpty { null }
        val value = resolver.resolve(PlaceholderQuery(type, target, property))
        return Tag.selfClosingInserting(Component.text(value))
    }
}
