package app.simplecloud.plugin.api.shared.placeholder.argument

import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue

/**
 * @author Niklas Nieberler
 */

interface ArgumentsResolver {

    /**
     * Gets the key for the placeholder
     */
    fun getKey(): String

    /**
     * Gets the resolved tag for the placeholder
     * @param arguments of the placeholder key
     */
    suspend fun resolve(arguments: ArgumentQueue): Tag?

}