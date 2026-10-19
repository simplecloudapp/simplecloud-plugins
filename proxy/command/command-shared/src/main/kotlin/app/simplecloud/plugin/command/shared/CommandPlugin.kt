package app.simplecloud.plugin.command.shared

import app.simplecloud.api.CloudApi
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.command.shared.config.DefaultConfigInstaller
import app.simplecloud.plugin.command.shared.config.MessageConfig
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File

/**
 * @author Fynn Bauer in 2024
 */
class CommandPlugin(private val dir: File) {

    private val logger = LoggerFactory.getLogger(CommandPlugin::class.java)

    val config = ConfigurationFactory(File(dir, "messages.yml"), MessageConfig::class.java)
    val messageConfiguration: MessageConfig get() = config.get()

    val api: CloudApi = CloudApi.create()

    // coroutine scope used for commands
    val scope = CoroutineScope(
        CoroutineName("simplecloud-command") +
                SupervisorJob() +
                Dispatchers.IO +
                CoroutineExceptionHandler { context, exception ->
                    logger.error("An error occurred in coroutine ${context[CoroutineName]?.name}", exception)
                }
    )

    fun startup() {
        DefaultConfigInstaller.install(File(dir, "messages.yml").toPath(), javaClass.classLoader)
        config.loadOrCreate(MessageConfig())
    }

    fun shutdown() {
        scope.cancel()
        api.close()
    }

}
