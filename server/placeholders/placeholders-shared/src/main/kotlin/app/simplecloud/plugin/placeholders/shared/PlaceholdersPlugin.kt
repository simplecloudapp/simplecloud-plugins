package app.simplecloud.plugin.placeholders.shared

import app.simplecloud.api.CloudApi
import app.simplecloud.api.CloudApiOptions
import app.simplecloud.plugin.placeholders.shared.placeholder.GroupPlaceholders
import app.simplecloud.plugin.placeholders.shared.placeholder.NetworkPlaceholders
import app.simplecloud.plugin.placeholders.shared.placeholder.PersistentServerPlaceholders
import app.simplecloud.plugin.placeholders.shared.placeholder.PlaceholderType
import app.simplecloud.plugin.placeholders.shared.placeholder.ServerPlaceholders
import app.simplecloud.plugin.placeholders.shared.query.PlaceholderQueryParser
import app.simplecloud.plugin.placeholders.shared.resolve.PlaceholderResolver

class PlaceholdersPlugin {

    private val api: CloudApi = CloudApi.create(CloudApiOptions.builder().component("placeholders").build())

    val types: List<PlaceholderType<*>> = listOf(PersistentServerPlaceholders, ServerPlaceholders, GroupPlaceholders, NetworkPlaceholders)
    val resolver = PlaceholderResolver(api, PlaceholderQueryParser(types))

    fun start() {
        resolver.preloadCurrent(types)
    }

    fun shutdown() {
        resolver.shutdown()
        api.close()
    }
}
