package app.simplecloud.plugin.api.shared.placeholder.async

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * @author Niklas Nieberler
 */

/**
 * This creates a new placeholder that can also be used asynchronously
 * @param key placeholder name
 * @param handler value for this placeholder
 */
data class AsyncPlaceholder<T>(
    val key: String,
    private val handler: AsyncPlaceholderHandler<T>
) {

    /**
     * Executes the handler method to get the appropriate value for this placeholder
     * @return placeholder value
     */
    suspend fun invoke(value: T): Any? {
        return this.handler.handle(value)
    }

    /**
     * Gets a matching [TagResolver] with matching key and value
     * @param value for the placeholder
     * @param prefix first name for the placeholder key
     */
    suspend fun invokeTagResolver(value: T, prefix: String? = null): TagResolver {
        val resolvedKey = prefix?.let { "${it}_$key" } ?: this.key
        return Placeholder.unparsed(resolvedKey, invoke(value).toString())
    }

}