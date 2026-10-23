package app.simplecloud.plugin.prefixes.shared.sync

import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.prefixes.shared.PrefixesConstants
import app.simplecloud.plugin.prefixes.shared.config.PrefixesConfig
import app.simplecloud.plugin.prefixes.shared.sync.tablist.SyncedTablist
import app.simplecloud.plugin.prefixes.shared.utilities.PrefixesCoroutineDetails
import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

class PrefixesSync(
    config: ConfigurationFactory<PrefixesConfig>,
) {

    private val logger = PrefixesConstants.LOGGER
    private val connection = createNatsConnection()
    private val subjects = PrefixesSubjects()
    private val subscriber = SyncSubscriber(connection, subjects, config)

    val publisher = SyncPublisher(connection, subjects, config)

    fun start(tablist: SyncedTablist<*, *>, target: SyncTarget) {
        subscriber.start(tablist, target)
        publisher.publishTablistRequest()

        PrefixesConstants.SCOPE.launch(PrefixesCoroutineDetails(null, "publish tablist")) {
            while (isActive) {
                delay(30.seconds)
                target.publishAll()
                tablist.expire()
            }
        }
    }

    fun shutdown() {
        runCatching {
            logger.info("Shutdown prefixes sync...")
            connection.flush(Duration.ofSeconds(1))
            subscriber.close()
            connection.close()
        }.onFailure { throwable ->
            logger.error("Failed to shutdown sync", throwable)
        }.onSuccess {
            logger.info("Successfully shutdown sync")
        }
    }

    private fun createNatsConnection(): Connection {
        val nats = Nats.connect(
            Options.builder()
                .server(System.getenv().getOrDefault("SIMPLECLOUD_NATS_URL", "wss://nats.simplecloud.app:443"))
                .userInfo(System.getenv().getOrDefault("SIMPLECLOUD_NETWORK_ID", "default"), System.getenv().getOrDefault("SIMPLECLOUD_NETWORK_SECRET", ""))
                .maxReconnects(-1)
                .build(),
        )

        logger.info("Connected to NATS server (${nats.connectedUrl})")
        return nats
    }
}
