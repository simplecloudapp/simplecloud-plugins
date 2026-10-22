package app.simplecloud.plugin.prefixes.api

import org.jetbrains.annotations.ApiStatus

/**
 * Provides access to the [PrefixesApi] instance.
 */
public object PrefixesApiProvider {

    @Volatile
    private var instance: PrefixesApi? = null

    /** Returns the [PrefixesApi] instance, or throws if the Prefixes plugin isn't loaded yet. */
    @JvmStatic
    public fun get(): PrefixesApi = instance ?: error(
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
    public fun register(api: PrefixesApi) {
        instance = api
    }

    @ApiStatus.Internal
    @JvmStatic
    public fun unregister() {
        instance = null
    }
}
