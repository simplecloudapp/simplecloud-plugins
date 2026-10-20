package app.simplecloud.plugin.prefixes.api

import org.jetbrains.annotations.ApiStatus

object PrefixesApiProvider {

    @Volatile
    private var instance: PrefixesApi? = null

    @JvmStatic
    fun get(): PrefixesApi = instance ?: throw IllegalStateException("PrefixesApi is not loaded yet!")

    @ApiStatus.Internal
    @JvmStatic
    fun register(api: PrefixesApi) {
        instance = api
    }

    @ApiStatus.Internal
    @JvmStatic
    fun unregister() {
        instance = null
    }
}
