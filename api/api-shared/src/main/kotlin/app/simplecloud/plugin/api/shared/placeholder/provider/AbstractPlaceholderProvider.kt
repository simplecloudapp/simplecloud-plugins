package app.simplecloud.plugin.api.shared.placeholder.provider

import app.simplecloud.api.CloudApi
import app.simplecloud.plugin.api.shared.extension.text
import app.simplecloud.plugin.api.shared.placeholder.argument.ArgumentsResolver
import app.simplecloud.plugin.api.shared.placeholder.single.SinglePlaceholderExecutor
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * @author Niklas Nieberler
 */

abstract class AbstractPlaceholderProvider<T>(
    private val executor: SinglePlaceholderExecutor<T>,
) {

    private val cloudApi = CloudApi.create()

    /**
     * Gets the list of all available [ArgumentsResolver]
     * @param cloudApi the instance of [CloudApi]
     * @param value for the placeholder
     */
    abstract suspend fun getArgumentsResolvers(
        cloudApi: CloudApi,
        value: T,
    ): List<ArgumentsResolver>

    /**
     * Gets the sum of all [TagResolver]
     * @param values for the placeholder
     * @param prefix of the placeholder key
     */
    suspend fun getTagResolver(
        values: List<T>,
        prefix: String? = null,
        vararg argumentsResolver: ArgumentsResolver,
    ): TagResolver {
        val availableArgumentsResolver = buildList {
            addAll(values.flatMap { getArgumentsResolvers(cloudApi, it) })
            addAll(argumentsResolver)
        }
        val singleTagResolver = values.map { this.executor.getTagResolver(this.cloudApi, it, prefix) }
        return TagResolver.resolver(
            *singleTagResolver.toTypedArray(),
            *availableArgumentsResolver
                .map { convertArgumentsResolverToTagResolver(it, prefix) }
                .toTypedArray()
        )
    }

    /**
     * Serializes the string to a [Component]
     * @param value for the placeholder
     * @param string the message
     * @param prefix of the placeholder key
     */
    suspend fun append(
        value: T,
        string: String,
        prefix: String? = null,
        vararg argumentsResolver: ArgumentsResolver,
    ): Component {
        return text(
            string,
            getTagResolver(listOf(value), prefix, *argumentsResolver),
        )
    }

    /**
     * Serializes the string to a [Component]
     * @param values for the placeholder
     * @param string the message
     * @param prefix of the placeholder key
     */
    suspend fun append(
        values: List<T>,
        string: String,
        prefix: String? = null,
        vararg argumentsResolver: ArgumentsResolver,
    ): Component {
        return text(
            string,
            getTagResolver(values, prefix, *argumentsResolver),
        )
    }

    private fun convertArgumentsResolverToTagResolver(resolver: ArgumentsResolver, prefix: String?): TagResolver {
        val key = resolver.getKey()
        val resolvedKey = prefix?.let { "${it}_$key" } ?: key
        return TagResolver.resolver(resolvedKey) { arguments, _ ->
            return@resolver runBlocking { resolver.resolve(arguments) as Tag }
        }
    }

}