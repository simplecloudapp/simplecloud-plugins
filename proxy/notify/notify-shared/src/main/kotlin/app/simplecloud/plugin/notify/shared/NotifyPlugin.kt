package app.simplecloud.plugin.notify.shared

import app.simplecloud.api.CloudApi
import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.plugin.notify.shared.config.DefaultConfigInstaller
import app.simplecloud.plugin.notify.shared.config.MessageConfig
import app.simplecloud.plugin.notify.shared.listener.NotificationListener
import app.simplecloud.plugin.notify.shared.utilities.MessageFormatter
import app.simplecloud.plugin.notify.shared.notification.NotificationCache
import app.simplecloud.plugin.notify.shared.platform.NotifyPlatform
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import java.util.UUID

class NotifyPlugin(private val platform: NotifyPlatform) {

    private val logger = LoggerFactory.getLogger(NotifyPlugin::class.java)

    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val api = CloudApi.create()
    val messages = createMessages()

    val formatter = MessageFormatter(messages)
    val cache = NotificationCache(api.player())
    private val listener = NotificationListener(api, messages, platform, formatter, cache)

    fun startup() {
        listener.start()
    }

    fun shutdown() {
        listener.stop()
        cache.clear()
        scope.cancel()
    }

    fun onPlayerJoin(id: UUID) {
        scope.launch {
            cache.load(id)
        }
    }

    fun onPlayerQuit(id: UUID) {
        cache.unload(id)
    }

    fun reload(): Boolean {
        return runCatching {
            logger.info("Reloading simplecloud notify...")
            messages.reload()
        }.onSuccess {
            logger.info("Successfully reloaded simplecloud notify")
        }.onFailure { throwable ->
            logger.error("Failed to reload simplecloud notify", throwable)
        }.isSuccess
    }

    private fun createMessages(): ConfigurationFactory<MessageConfig> {
        val file = File(platform.getDataDirectory(), "messages.yml")
        DefaultConfigInstaller.install(file.toPath(), javaClass.classLoader)

        val factory = ConfigurationFactory(file, MessageConfig::class.java)
        factory.loadOrCreate(MessageConfig())
        return factory
    }

}
