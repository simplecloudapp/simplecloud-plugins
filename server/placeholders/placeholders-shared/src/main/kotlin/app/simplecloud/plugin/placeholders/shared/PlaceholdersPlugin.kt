package app.simplecloud.plugin.placeholders.shared

import app.simplecloud.api.CloudApi
import app.simplecloud.api.CloudApiOptions
import app.simplecloud.plugin.placeholders.shared.placeholder.GroupPlaceholders
import app.simplecloud.plugin.placeholders.shared.placeholder.PersistentServerPlaceholders
import app.simplecloud.plugin.placeholders.shared.placeholder.ServerPlaceholders
import app.simplecloud.plugin.placeholders.shared.resolve.PlaceholderResolver
import kotlinx.coroutines.*

class PlaceholdersPlugin {

    private val api: CloudApi = CloudApi.create(CloudApiOptions.builder().component("placeholders").build())
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val resolver = PlaceholderResolver(api, scope)

    fun start() {
        resolver.preloadCurrent(listOf(ServerPlaceholders, GroupPlaceholders, PersistentServerPlaceholders))
    }

    fun shutdown() {
        scope.cancel()
        api.close()
    }
}
