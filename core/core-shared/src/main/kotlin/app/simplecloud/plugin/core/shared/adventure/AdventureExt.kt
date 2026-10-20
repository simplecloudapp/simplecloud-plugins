package app.simplecloud.plugin.core.shared.adventure

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

val miniMessage = MiniMessage.miniMessage()

private val playerNameRegex = Regex("""^[^<>:'"\\\s]{1,32}$""")

fun text(message: String, vararg tagResolver: TagResolver): Component = miniMessage.deserialize(message, *tagResolver)

fun tags(vararg entries: Pair<String, Any?>): TagResolver = TagResolver.resolver(
    entries.map { (key, value) ->
        val text = value?.toString().orEmpty()
        if (key == "playername" && playerNameRegex.matches(text)) {
            Placeholder.parsed(key, text)
        } else {
            Placeholder.unparsed(key, text)
        }
    },
)
