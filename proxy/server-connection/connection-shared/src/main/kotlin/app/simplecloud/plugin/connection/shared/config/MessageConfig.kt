package app.simplecloud.plugin.connection.shared.config

import app.simplecloud.plugin.connection.shared.utilities.config.ConfigVersion
import app.simplecloud.core.shared.config.VersionedConfig
import app.simplecloud.core.shared.config.message.AbstractMessageConfig
import app.simplecloud.core.shared.config.message.MessagePalette
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MessageConfig(
    override val version: Int = ConfigVersion.VERSION,
    val palette: Map<String, String> = MessagePalette.defaults,
    val variables: Map<String, String> = mapOf("prefix" to MessagePalette.PREFIX),
    val kick: KickMessages = KickMessages(),
) : VersionedConfig, AbstractMessageConfig(palette, variables)

@ConfigSerializable
data class KickMessages(
    val noFallbackServers: String =
        "<error>There is no server to send you to.<br><br><reason><br><muted>Try again in a few seconds.",
    val noTargetConnection: String =
        "<error>There is no server to join right now.<br><br><muted>Try again in a few seconds.",
)
