package app.simplecloud.plugin.core.shared.command

import net.kyori.adventure.text.Component
import java.util.UUID

/** A command sender, e.g. a player or the console. */
interface CloudSender {

    /** The unique id of the player, or `null` if the sender is not a player. */
    fun getUniqueId(): UUID?

    /** Whether the sender has the given [permission]. */
    fun hasPermission(permission: String): Boolean

    /** Sends the [message] to the sender. */
    fun sendMessage(message: Component)

    /** Whether the sender is a player. */
    fun isPlayer(): Boolean = getUniqueId() != null
}
