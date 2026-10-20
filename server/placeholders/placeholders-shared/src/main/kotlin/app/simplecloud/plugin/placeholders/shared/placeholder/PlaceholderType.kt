package app.simplecloud.plugin.placeholders.shared.placeholder

import app.simplecloud.api.CloudApi

interface PlaceholderType<T : Any> {

    val prefix: String

    val properties: Set<String>

    fun getValue(resource: T, property: String): Any?

    suspend fun loadCurrent(api: CloudApi): T?

    suspend fun load(api: CloudApi, target: String): T?
}
