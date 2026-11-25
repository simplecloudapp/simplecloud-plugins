package app.simplecloud.plugin.command.shared

import app.simplecloud.api.CloudApi
import app.simplecloud.api.CloudApiOptions
import app.simplecloud.plugin.command.shared.config.MessageConfig
import app.simplecloud.plugin.command.shared.config.migration.MessageMigrations
import app.simplecloud.plugin.command.shared.utilities.Formatter
import app.simplecloud.core.shared.config.ConfigWatcher
import app.simplecloud.core.shared.config.ConfigurationFactory
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File

class CommandPlugin(private val dir: File) {

    private val logger = LoggerFactory.getLogger(CommandPlugin::class.java)

    val api: CloudApi = CloudApi.create(CloudApiOptions.builder().component("cloud-command").build())

    val configuration = ConfigurationFactory.create(dir, "messages.yml", MessageConfig(), MessageMigrations)
    val config: MessageConfig get() = configuration.get()
    val formatter: Formatter get() = Formatter(config.format)

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
        ConfigWatcher(dir.toPath()) { configuration.reload() }.start(scope)
    }

    fun shutdown() {
        scope.cancel()
        api.close()
    }
}
