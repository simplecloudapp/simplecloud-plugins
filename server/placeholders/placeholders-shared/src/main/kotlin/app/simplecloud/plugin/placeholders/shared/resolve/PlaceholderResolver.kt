package app.simplecloud.plugin.placeholders.shared.resolve

import app.simplecloud.api.CloudApi
import app.simplecloud.plugin.placeholders.shared.placeholder.PlaceholderType
import app.simplecloud.plugin.placeholders.shared.query.PlaceholderQuery
import app.simplecloud.plugin.placeholders.shared.query.PlaceholderQueryParser
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class PlaceholderResolver(
    private val api: CloudApi,
    private val scope: CoroutineScope,
    private val parser: PlaceholderQueryParser = PlaceholderQueryParser(),
    private val ttl: Duration = 5.seconds,
) {

    private val cache = ConcurrentHashMap<ResourceKey, CachedResource>()
    private val loading = ConcurrentHashMap.newKeySet<ResourceKey>()

    fun resolve(params: String): String? {
        val query = parser.parse(params) ?: return null
        return resolve(query)
    }

    fun resolve(query: PlaceholderQuery): String {
        val key = ResourceKey(query.type, query.target)
        val cached = cache[key]
        if (cached == null || cached.loadedAt.elapsedNow() > ttl) refresh(key)

        val resource = cached?.resource ?: return ""
        return query.type.read(resource, query.property)?.toString().orEmpty()
    }

    fun preloadCurrent(types: List<PlaceholderType<*>>) {
        types.forEach { refresh(ResourceKey(it, null)) }
    }

    private fun refresh(key: ResourceKey) {
        if (!loading.add(key)) return

        scope.launch {
            try {
                cache[key] = CachedResource(load(key), TimeSource.Monotonic.markNow())
            } finally {
                loading.remove(key)
            }
        }
    }

    private suspend fun load(key: ResourceKey): Any? = try {
        if (key.target == null) key.type.loadCurrent(api) else key.type.load(api, key.target)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    @Suppress("UNCHECKED_CAST")
    private fun PlaceholderType<*>.read(resource: Any, property: String): Any? =
        (this as PlaceholderType<Any>).getValue(resource, property)

    private data class ResourceKey(val type: PlaceholderType<*>, val target: String?)

    private class CachedResource(val resource: Any?, val loadedAt: TimeMark)
}
