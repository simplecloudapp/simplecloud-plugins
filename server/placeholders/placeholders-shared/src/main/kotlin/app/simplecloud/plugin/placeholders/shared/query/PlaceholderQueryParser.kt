package app.simplecloud.plugin.placeholders.shared.query

import app.simplecloud.plugin.placeholders.shared.placeholder.GroupPlaceholders
import app.simplecloud.plugin.placeholders.shared.placeholder.NetworkPlaceholders
import app.simplecloud.plugin.placeholders.shared.placeholder.PersistentServerPlaceholders
import app.simplecloud.plugin.placeholders.shared.placeholder.PlaceholderType
import app.simplecloud.plugin.placeholders.shared.placeholder.ServerPlaceholders

class PlaceholderQueryParser(private val types: List<PlaceholderType<*>> = DEFAULT_TYPES) {

    fun parse(params: String): PlaceholderQuery? {
        for (type in types) {
            val prefix = "${type.prefix}_"
            if (!params.startsWith(prefix)) continue

            val rest = params.removePrefix(prefix)
            val property = findProperty(type, rest) ?: continue
            val target = rest.removeSuffix(property).removeSuffix("_").ifEmpty { null }
            return PlaceholderQuery(type, target, property)
        }
        return null
    }

    private fun findProperty(type: PlaceholderType<*>, rest: String): String? = type.properties
        .sortedByDescending { it.length }
        .firstOrNull { rest == it || rest.endsWith("_$it") }

    private companion object {
        val DEFAULT_TYPES = listOf(PersistentServerPlaceholders, ServerPlaceholders, GroupPlaceholders, NetworkPlaceholders)
    }
}
