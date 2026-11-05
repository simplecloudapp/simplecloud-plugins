package app.simplecloud.plugin.command.shared

import app.simplecloud.api.CloudApi
import app.simplecloud.plugin.command.shared.config.MessageConfig
import app.simplecloud.plugin.core.shared.config.ConfigWatcher
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File

class CommandPlugin(private val dir: File) {

    private val logger = LoggerFactory.getLogger(CommandPlugin::class.java)

    val config = ConfigurationFactory.create(dir, "messages.yml", MessageConfig())
    val messageConfiguration: MessageConfig get() = config.get()

    val api: CloudApi = CloudApi.create()

    // coroutine scope used for commands
    val scope = CoroutineScope(
        CoroutineName("simplecloud-command") +
            SupervisorJob() +
            Dispatchers.IO +
            CoroutineExceptionHandler { context, exception ->
                logger.error("An error occurred in coroutine ${context[CoroutineName]?.name}", exception)
            },
    )

    fun start() {
        ConfigWatcher(dir.toPath()) { config.reload() }.start(scope)
    }

    fun shutdown() {
        scope.cancel()
        api.close()
    }
}
