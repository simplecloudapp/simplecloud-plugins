package app.simplecloud.plugin.prefixes.shared.sync

import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.prefixes.shared.PrefixesConstants
import app.simplecloud.plugin.prefixes.shared.config.PrefixesConfig
import app.simplecloud.plugin.prefixes.shared.sync.tablist.SyncedTablist
import app.simplecloud.plugin.prefixes.shared.sync.tablist.TablistEntryMapper
import app.simplecloud.plugin.prefixes.shared.utilities.ComponentSerializer
import app.simplecloud.plugin.prefixes.v1.ChatMessageEvent
import app.simplecloud.plugin.prefixes.v1.TablistEntryRemoveEvent
import app.simplecloud.plugin.prefixes.v1.TablistEntryUpdateEvent
import io.nats.client.Connection
import io.nats.client.Message
import java.time.Duration
import java.util.UUID

class SyncSubscriber(
    connection: Connection,
    private val subjects: PrefixesSubjects,
    private val config: ConfigurationFactory<PrefixesConfig>,
) {
    private val logger = PrefixesConstants.LOGGER
    private val dispatcher = connection.createDispatcher(null)

    fun start(tablist: SyncedTablist<*, *>, target: SyncTarget) {
        subscribe(PrefixesConfig::isChatSynced, PrefixesSubjects.CHAT) { message ->
            target.broadcast(ComponentSerializer.deserialize(ChatMessageEvent.parseFrom(message.data).json))
        }
        subscribe(PrefixesConfig::isTablistSynced, PrefixesSubjects.TABLIST_UPDATE) { message ->
            tablist.update(
                subjects.publisherId(message.subject, PrefixesSubjects.TABLIST_UPDATE),
                TablistEntryMapper.fromDefinition(TablistEntryUpdateEvent.parseFrom(message.data)),
            )
        }
        subscribe(PrefixesConfig::isTablistSynced, PrefixesSubjects.TABLIST_REMOVE) { message ->
            tablist.remove(
                subjects.publisherId(message.subject, PrefixesSubjects.TABLIST_REMOVE),
                UUID.fromString(TablistEntryRemoveEvent.parseFrom(message.data).playerId),
            )
        }
        subscribe(PrefixesConfig::isTablistSynced, PrefixesSubjects.TABLIST_REQUEST) {
            target.publishAll()
        }
    }

    fun close() {
        try {
            dispatcher.drain(Duration.ofSeconds(1))
        } catch (e: Exception) {
            logger.error("Failed to drain prefixes sync dispatcher", e)
        }
    }

    private fun subscribe(enabled: (PrefixesConfig) -> Boolean, subject: String, handler: (Message) -> Unit) {
        subjects.patterns(config.get().sync.sources, subject).forEach { pattern ->
            runCatching { subscribe(pattern, enabled, handler) }.onFailure { throwable ->
                logger.error("Failed to subscribe to the sync subject '$pattern'", throwable)
            }
        }
    }

    private fun subscribe(pattern: String, enabled: (PrefixesConfig) -> Boolean, handler: (Message) -> Unit) {
        dispatcher.subscribe(pattern) { message ->
            if (!enabled(config.get()) || subjects.isOwn(message.subject)) return@subscribe

            try {
                handler(message)
            } catch (e: Exception) {
                logger.error("Failed to handle sync message on '${message.subject}'", e)
            }
        }
    }
}
