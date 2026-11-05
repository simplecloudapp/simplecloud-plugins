package app.simplecloud.plugin.connection.shared.config

import app.simplecloud.plugin.connection.shared.utilities.config.ConfigVersion
import app.simplecloud.plugin.core.shared.config.AbstractMessageConfig
import app.simplecloud.plugin.core.shared.config.MessagePalette
import app.simplecloud.plugin.core.shared.config.VersionedConfig
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MessageConfig(
    override val version: Int = ConfigVersion.VERSION,
    override val palette: Map<String, String> = MessagePalette.defaults,
    override val variables: Map<String, String> = mapOf("prefix" to MessagePalette.PREFIX),
    val kick: KickMessages = KickMessages(),
    val command: ConnectionCommandMessages = ConnectionCommandMessages(),
) : VersionedConfig, AbstractMessageConfig()

@ConfigSerializable
data class KickMessages(
    val noFallbackServers: String = "<error>There is no server to send you to.<br><br><muted>Try again in a few seconds.",
    val noTargetConnection: String = "<error>There is no server to join right now.<br><br><muted>Try again in a few seconds.",
)

@ConfigSerializable
data class ConnectionCommandMessages(
    val commandUsage: String = "<prefix> <info>Use /connection reload to reload the configuration.",
    val configReloading: String = "<prefix> <info>Reloading the connection configuration.",
    val configReloadedSuccess: String = "<prefix> <success>Reloaded the connection configuration!",
    val configReloadedFailed: String = "<prefix> <error>Couldn't reload the connection configuration. Check the console.",
)
