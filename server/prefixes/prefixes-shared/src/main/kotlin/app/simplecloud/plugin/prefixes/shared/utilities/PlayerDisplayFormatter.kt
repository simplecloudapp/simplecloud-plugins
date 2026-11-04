package app.simplecloud.plugin.prefixes.shared.utilities

import app.simplecloud.plugin.core.shared.adventure.miniMessage
import app.simplecloud.plugin.prefixes.api.group.PrefixesPlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object PlayerDisplayFormatter {

    fun formatDisplayName(data: PrefixesPlayerData, playerName: String, displayNameEnabled: Boolean): Component = if (displayNameEnabled) data.displayName else Component.text(playerName)

    fun formatTablistName(data: PrefixesPlayerData, playerName: String): Component = Component.text(playerName, data.color)

    fun formatChatMessage(data: PrefixesPlayerData, playerName: String, message: Component, displayNameEnabled: Boolean): Component = miniMessage.deserialize(
        data.chatFormat,
        Placeholder.component("prefix", data.prefix),
        Placeholder.component("suffix", data.suffix),
        Placeholder.styling("color", data.color),
        Placeholder.unparsed("playername", playerName),
        Placeholder.unparsed("name", playerName),
        Placeholder.component("displayname", formatDisplayName(data, playerName, displayNameEnabled)),
        Placeholder.component("message", message),
    )
}
