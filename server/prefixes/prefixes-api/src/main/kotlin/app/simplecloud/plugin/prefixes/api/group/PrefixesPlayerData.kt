package app.simplecloud.plugin.prefixes.api.group

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

/**
 * Represents the resolved prefixes data of a player.
 *
 * @property prefix The prefix of the player.
 * @property suffix The suffix of the player.
 * @property color The color of the player, used in the tablist and by the color placeholder.
 * @property displayName The display name of the player, shown above the head.
 * @property chatFormat The chat format of the player.
 * @property priority The priority of the player, used to sort the tablist.
 */
public data class PrefixesPlayerData(
    val prefix: Component,
    val suffix: Component,
    val color: TextColor,
    val displayName: Component,
    val chatFormat: String,
    val priority: Int,
)
