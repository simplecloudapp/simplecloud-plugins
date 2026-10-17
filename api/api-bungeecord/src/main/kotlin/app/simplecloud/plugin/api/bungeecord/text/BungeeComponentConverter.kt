package app.simplecloud.plugin.api.bungeecord.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer

/**
 * Converts between Adventure and BungeeCord components.
 */
object BungeeComponentConverter {

    private val serializer = GsonComponentSerializer.gson()

    /**
     * Converts the given [component] into a [BaseComponent].
     */
    fun toBungee(component: Component): BaseComponent =
        ComponentSerializer.deserialize(serializer.serialize(component))

    /**
     * Converts the given [component] into an Adventure [Component].
     */
    fun fromBungee(component: BaseComponent): Component =
        serializer.deserialize(ComponentSerializer.toString(component))
}
