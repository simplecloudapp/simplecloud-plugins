package app.simplecloud.plugin.placeholders.shared.query

import app.simplecloud.plugin.placeholders.shared.placeholder.PlaceholderType

data class PlaceholderQuery(
    val type: PlaceholderType<*>,
    val target: String?,
    val property: String,
)
