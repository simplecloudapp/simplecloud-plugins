package app.simplecloud.plugin.prefixes.shared.sync

import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.prefixes.shared.PrefixesConstants
import app.simplecloud.plugin.prefixes.shared.config.PrefixesConfig
import app.simplecloud.plugin.prefixes.shared.sync.tablist.TablistEntry
import app.simplecloud.plugin.prefixes.shared.sync.tablist.TablistEntryMapper
import app.simplecloud.plugin.prefixes.shared.utilities.ComponentSerializer
import app.simplecloud.plugin.prefixes.v1.ChatMessageEvent
import app.simplecloud.plugin.prefixes.v1.TablistEntryRemoveEvent
import app.simplecloud.plugin.prefixes.v1.TablistSyncRequest
import com.google.protobuf.MessageLite
import io.nats.client.Connection
import net.kyori.adventure.text.Component
import java.util.UUID

class SyncPublisher(
    private val connection: Connection,
    private val subjects: PrefixesSubjects,
    private val config: ConfigurationFactory<PrefixesConfig>,
) {
    private val logger = PrefixesConstants.LOGGER

    fun publishChatMessage(message: Component) = publish(PrefixesConfig::isChatSynced, PrefixesSubjects.CHAT) {
        ChatMessageEvent.newBuilder()
            .setJson(ComponentSerializer.serialize(message))
            .build()
    }

    fun publishTablistEntry(entry: TablistEntry) = publish(PrefixesConfig::isTablistSynced, PrefixesSubjects.TABLIST_UPDATE) {
        TablistEntryMapper.toDefinition(entry)
    }

    fun publishTablistRemove(uniqueId: UUID) = publish({ true }, PrefixesSubjects.TABLIST_REMOVE) {
        TablistEntryRemoveEvent.newBuilder()
            .setPlayerId(uniqueId.toString())
            .build()
    }

    fun publishTablistRequest() = publish(PrefixesConfig::isTablistSynced, PrefixesSubjects.TABLIST_REQUEST) {
        TablistSyncRequest.getDefaultInstance()
    }

    private fun publish(enabled: (PrefixesConfig) -> Boolean, subject: String, message: () -> MessageLite) {
        if (!enabled(config.get())) return

        try {
            connection.publish(subjects.own(subject), message().toByteArray())
        } catch (e: Exception) {
            logger.error("Failed to publish sync message on '$subject'", e)
        }
    }
}
