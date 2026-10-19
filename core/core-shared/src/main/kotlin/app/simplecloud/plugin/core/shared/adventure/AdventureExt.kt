package app.simplecloud.plugin.core.shared.adventure

import app.simplecloud.api.group.Group
import app.simplecloud.api.persistentserver.PersistentServer
import app.simplecloud.api.server.Server
import app.simplecloud.plugin.core.shared.placeholder.PlaceholderProvider
import app.simplecloud.plugin.core.shared.placeholder.argument.ArgumentsResolver
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

suspend fun String.appendToComponent(
    server: Server,
    prefix: String? = null,
    vararg argumentsResolver: ArgumentsResolver,
): Component = PlaceholderProvider.serverPlaceholderProvider.append(
    server,
    this,
    prefix,
    *argumentsResolver,
)

suspend fun String.appendToComponent(
    group: Group,
    prefix: String? = null,
    vararg argumentsResolver: ArgumentsResolver,
): Component = PlaceholderProvider.groupPlaceholderProvider.append(
    group,
    this,
    prefix,
    *argumentsResolver,
)

suspend fun String.appendToComponent(
    persistentServer: PersistentServer,
    prefix: String? = null,
    vararg argumentsResolver: ArgumentsResolver,
): Component = PlaceholderProvider.persistentServerPlaceholderProvider.append(
    persistentServer,
    this,
    prefix,
    *argumentsResolver,
)
