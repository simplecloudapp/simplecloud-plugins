package app.simplecloud.plugin.placeholders.shared.resolve

import app.simplecloud.api.CloudApi
import app.simplecloud.plugin.placeholders.shared.placeholder.PlaceholderType
import app.simplecloud.plugin.placeholders.shared.query.PlaceholderQuery
import app.simplecloud.plugin.placeholders.shared.query.PlaceholderQueryParser
import com.github.benmanes.caffeine.cache.AsyncCacheLoader
import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class PlaceholderResolver(
    private val api: CloudApi,
    private val parser: PlaceholderQueryParser = PlaceholderQueryParser(),
    private val loadingValue: String = "...",
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val logger = LoggerFactory.getLogger(PlaceholderResolver::class.java)

    private val cache: AsyncLoadingCache<ResourceKey, CachedResource> = Caffeine.newBuilder()
        .maximumSize(1_000L)
        .refreshAfterWrite(5.seconds.toJavaDuration())
        .expireAfterAccess(5.minutes.toJavaDuration())
        .buildAsync(AsyncCacheLoader { key, _ -> scope.future { CachedResource(load(key)) } })

    fun resolve(params: String): String? {
        val query = parser.parse(params) ?: return null
        return resolve(query)
    }

    fun resolve(query: PlaceholderQuery): String {
        val future = cache.get(ResourceKey(query.type, query.target))
        if (!future.isDone) return loadingValue

        val resource = future.exceptionally { null }.getNow(null)?.resource ?: return ""

        @Suppress("UNCHECKED_CAST")
        val type = query.type as PlaceholderType<Any>
        return type.getValue(resource, query.property)?.toString().orEmpty()
    }

    @Suppress("IgnoredReturnValue")
    fun preloadCurrent(types: List<PlaceholderType<*>>) {
        types.forEach { cache.get(ResourceKey(it, null)) }
    }

    fun shutdown() {
        scope.cancel()
    }

    private suspend fun load(key: ResourceKey): Any? = try {
        if (key.target == null) key.type.loadCurrent(api) else key.type.load(api, key.target)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.warn("Failed to load {} placeholder for target {}", key.type.prefix, key.target ?: "<current>", e)
        null
    }

    private data class ResourceKey(val type: PlaceholderType<*>, val target: String?)

    private class CachedResource(val resource: Any?)
}
