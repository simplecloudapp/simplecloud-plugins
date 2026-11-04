package app.simplecloud.npc.core.cloud

import app.simplecloud.npc.core.text.NpcPalette
import app.simplecloud.plugin.api.shared.placeholder.PlaceholderProvider
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object BridgePlaceholders {
    /** Parses [text] with the palette tags and the placeholders of [bridge], e.g. <target_name>. */
    suspend fun append(bridge: ServerBridge, text: String, prefix: String = "target"): Component =
        NpcPalette.miniMessage.deserialize(text, resolver(bridge, prefix))

    suspend fun resolver(bridge: ServerBridge, prefix: String = "target"): TagResolver = when (bridge) {
        is ServerBridge.OfPersistentServer ->
            PlaceholderProvider.persistentServerPlaceholderProvider
                .getTagResolver(listOf(bridge.persistentServer), prefix)

        is ServerBridge.OfGroup -> PlaceholderProvider.groupPlaceholderProvider.getTagResolver(listOf(bridge.group), prefix)

        is ServerBridge.OfServer -> PlaceholderProvider.serverPlaceholderProvider.getTagResolver(listOf(bridge.server), prefix)
    }
}
