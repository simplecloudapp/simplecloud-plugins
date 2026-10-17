package app.simplecloud.plugin.api.shared.placeholder.single

import app.simplecloud.api.CloudApi
import app.simplecloud.plugin.api.shared.placeholder.async.AsyncPlaceholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * @author Niklas Nieberler
 */

interface SinglePlaceholderExecutor<T> {

    /**
     * Gets a list with all available [AsyncPlaceholder]
     * @param cloudApi the instance of [app.simplecloud.api.CloudApi]
     */
    fun getAsyncPlaceholders(cloudApi: CloudApi): List<AsyncPlaceholder<T>>

    /**
     * Gets a [TagResolver] with all available tag resolvers from the [getAsyncPlaceholders] method
     * @param cloudApi the instance of [CloudApi]
     * @param value for the placeholder
     * @param prefix first name for the placeholder key
     */
    suspend fun getTagResolver(
        cloudApi: CloudApi,
        value: T,
        prefix: String? = null,
    ): TagResolver {
        return TagResolver.resolver(getAsyncPlaceholders(cloudApi)
            .map { it.invokeTagResolver(value, prefix) })
    }

}