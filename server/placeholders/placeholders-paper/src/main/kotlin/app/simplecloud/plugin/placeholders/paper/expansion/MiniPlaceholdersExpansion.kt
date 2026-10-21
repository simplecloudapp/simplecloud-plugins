package app.simplecloud.plugin.placeholders.paper.expansion

import app.simplecloud.plugin.placeholders.shared.BuildConstants
import app.simplecloud.plugin.placeholders.shared.placeholder.PlaceholderType
import app.simplecloud.plugin.placeholders.shared.query.PlaceholderQuery
import app.simplecloud.plugin.placeholders.shared.resolve.PlaceholderResolver
import io.github.miniplaceholders.api.Expansion
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import org.bukkit.entity.Player
import java.util.UUID

class MiniPlaceholdersExpansion(
    private val resolver: PlaceholderResolver,
    types: List<PlaceholderType<*>>,
) {

    private val expansion = createExpansion(types)

    fun register() = expansion.register()

    fun unregister() {
        if (expansion.registered()) expansion.unregister()
    }

    private fun createExpansion(types: List<PlaceholderType<*>>): Expansion {
        val builder = Expansion.builder("simplecloud").author("SimpleCloud").version(BuildConstants.VERSION)
        types.forEach { type ->
            type.properties.forEach { property ->
                val key = "${type.prefix}_$property"
                if (type.isViewerScoped()) {
                    builder.audiencePlaceholder(Player::class.java, key) { player, queue, _ ->
                        resolve(type, property, queue, player.uniqueId)
                    }
                } else {
                    builder.globalPlaceholder(key) { queue, _ -> resolve(type, property, queue, null) }
                }
            }
        }
        return builder.build()
    }

    private fun resolve(type: PlaceholderType<*>, property: String, queue: ArgumentQueue, viewer: UUID?): Tag {
        val arguments = mutableListOf<String>()
        while (queue.hasNext()) arguments += queue.pop().value()

        val target = arguments.joinToString(":").ifEmpty { null }
        val value = resolver.resolve(PlaceholderQuery(type, target, property), viewer)
        return Tag.selfClosingInserting(Component.text(value))
    }
}
