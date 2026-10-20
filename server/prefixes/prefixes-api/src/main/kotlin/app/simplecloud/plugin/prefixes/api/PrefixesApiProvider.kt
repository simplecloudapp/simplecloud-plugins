package app.simplecloud.plugin.prefixes.api

import org.jetbrains.annotations.ApiStatus

object PrefixesApiProvider {

    @Volatile
    private var instance: PrefixesApi? = null

    @JvmStatic
    fun get(): PrefixesApi = instance ?: error(
        """
        The Prefixes API isn't loaded yet!
        This could be because:
          a) the Prefixes plugin is not installed or it failed to enable
          b) the plugin in the stacktrace does not declare a dependency on Prefixes
          c) the plugin in the stacktrace is retrieving the API before the plugin 'enable' phase
          d) the plugin in the stacktrace is incorrectly 'shading' the Prefixes API into its jar
          e) on Minestom, PrefixesMinestom.builder(...).enable() was not called yet
        """.trimIndent(),
    )

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
