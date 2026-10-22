package app.simplecloud.plugin.prefixes.api.group

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Represents a prefixes group.
 */
public interface PrefixesGroup {

    /** The name of this group. */
    public val name: String

    /** The priority of this group, used to sort the tablist. */
    public val priority: Int

    /** The permission required to be a member of this group. */
    public val permission: String

    /** The prefix of this group. */
    public val prefix: Component

    /** The suffix of this group. */
    public val suffix: Component

    /** The color of this group, used in the tablist and by the color placeholder. */
    public val color: TextColor

    /** The display name format of this group, shown above the player's head. */
    public val displayName: String

    /** The chat format of this group. */
    public val chatFormat: String

    /** Checks whether [player] is a member of this group. */
    public fun containsPlayer(player: UUID): CompletableFuture<Boolean>
}
