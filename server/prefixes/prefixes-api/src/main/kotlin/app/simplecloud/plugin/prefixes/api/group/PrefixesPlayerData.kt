package app.simplecloud.plugin.prefixes.api.group

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

data class PrefixesPlayerData(
    val prefix: Component,
    val suffix: Component,
    val color: TextColor,
    val displayName: Component,
    val chatFormat: String,
    val priority: Int,
)
