package app.simplecloud.plugin.prefixes.shared

import app.simplecloud.plugin.core.shared.config.ConfigWatcher
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.prefixes.api.PrefixesApiProvider
import app.simplecloud.plugin.prefixes.shared.api.PrefixesApiImpl
import app.simplecloud.plugin.prefixes.shared.config.MessageConfig
import app.simplecloud.plugin.prefixes.shared.config.PrefixesConfig
import app.simplecloud.plugin.prefixes.shared.config.SyncConfig
import app.simplecloud.plugin.prefixes.shared.data.PrefixesDataRegistry
import app.simplecloud.plugin.prefixes.shared.group.GroupProviderRegistry
import app.simplecloud.plugin.prefixes.shared.group.config.ConfigGroupProvider
import app.simplecloud.plugin.prefixes.shared.group.luckperms.LuckPermsGroupProvider
import app.simplecloud.plugin.prefixes.shared.group.luckperms.LuckPermsListener
import app.simplecloud.plugin.prefixes.shared.platform.PrefixesListener
import app.simplecloud.plugin.prefixes.shared.platform.PrefixesPlatform
import app.simplecloud.plugin.prefixes.shared.sync.PrefixesSync
import app.simplecloud.plugin.prefixes.shared.utilities.ColorParser
import kotlinx.coroutines.cancelChildren
import net.luckperms.api.event.EventSubscription
import java.util.concurrent.CopyOnWriteArrayList

class Prefixes(private val platform: PrefixesPlatform) {

    private val logger = PrefixesConstants.LOGGER
    private val listeners = CopyOnWriteArrayList<PrefixesListener>()
    private val subscriptions = CopyOnWriteArrayList<EventSubscription<*>>()
    private val luckPerms = platform.getLuckPerms()

    val config = createConfig()
    val messages = createMessages()

    val registry = createGroupRegistry()
    val dataRegistry = PrefixesDataRegistry(platform)
    val api = PrefixesApiImpl(registry, dataRegistry, listeners, platform)
    val sync = createSync()

    fun startup() {
        PrefixesApiProvider.register(api)
        validateConfig()
        ConfigWatcher(platform.getDataDirectory().toPath()) { reload() }.start(PrefixesConstants.SCOPE)

        if (luckPerms != null) {
            subscriptions.addAll(LuckPermsListener(luckPerms, api).register())
        }
    }

    fun shutdown() {
        subscriptions.forEach { subscription -> subscription.close() }
        listeners.forEach(PrefixesListener::onShutdown)
        PrefixesApiProvider.unregister()
        sync?.shutdown()
        PrefixesConstants.SCOPE.coroutineContext.cancelChildren()
    }

    fun addListener(listener: PrefixesListener) {
        listeners.add(listener)
    }

    fun reload(): Boolean = runCatching {
        logger.info("Reloading simplecloud prefixes...")
        val previousSync = config.get().sync
        config.reload()
        messages.reload()
        validateConfig()
        warnIfRestartRequired(previousSync)
        listeners.forEach(PrefixesListener::onReload)
    }.onSuccess {
        logger.info("Successfully reloaded simplecloud prefixes")
    }.onFailure { throwable ->
        logger.error("Failed to reload simplecloud prefixes", throwable)
    }.isSuccess

    private fun validateConfig() {
        registry.validateSource()
        config.get().groups
            .filter { group -> group.color.isNotBlank() && ColorParser.parse(group.color) == null }
            .forEach { group -> logger.warn("The color '${group.color}' of the group '${group.name}' is invalid, using white instead") }
    }

    private fun warnIfRestartRequired(previous: SyncConfig) {
        val current = config.get().sync
        if ((sync == null && current.enabled) || previous.sources != current.sources) {
            logger.warn("Enabling sync or changing sync.sources only takes effect after a restart")
        }
    }

    private fun createConfig(): ConfigurationFactory<PrefixesConfig> =
        ConfigurationFactory.create(platform.getDataDirectory(), "config.yml", PrefixesConfig())

    private fun createMessages(): ConfigurationFactory<MessageConfig> =
        ConfigurationFactory.create(platform.getDataDirectory(), "messages.yml", MessageConfig())

    private fun createGroupRegistry(): GroupProviderRegistry {
        val registry = GroupProviderRegistry(config, ConfigGroupProvider(config, platform.getPermissionChecker()))

        if (luckPerms != null) {
            registry.register(LuckPermsGroupProvider(luckPerms))
        }

        return registry
    }

    private fun createSync(): PrefixesSync? {
        if (!config.get().sync.enabled) return null

        if (!isSimpleCloudAvailable()) {
            logger.warn("Sync is enabled in the config, but this Server is not running in a simplecloud network.")
            return null
        }

        return runCatching {
            PrefixesSync(config)
        }.onFailure { throwable ->
            logger.error("Failed to initialize sync", throwable)
        }.getOrNull()
    }

    private fun isSimpleCloudAvailable(): Boolean = System.getenv("SIMPLECLOUD_UNIQUE_ID") != null
}
