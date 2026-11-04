package app.simplecloud.plugin.prefixes.shared.config

import app.simplecloud.plugin.core.shared.config.AbstractCommandMessageConfig
import app.simplecloud.plugin.core.shared.config.AbstractCommandMessages
import app.simplecloud.plugin.core.shared.config.CommandExceptionMessages
import app.simplecloud.plugin.core.shared.config.MessagePalette
import app.simplecloud.plugin.core.shared.config.VersionedConfig
import app.simplecloud.plugin.prefixes.shared.utilities.config.ConfigVersion
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MessageConfig(
    override val version: Int = ConfigVersion.VERSION,
    override val palette: Map<String, String> = MessagePalette.defaults,
    override val variables: Map<String, String> = mapOf("prefix" to "<color:#38bdf8><bold>⚡</bold></color>"),
    override val command: CommandMessages = CommandMessages(),
) : VersionedConfig, AbstractCommandMessageConfig()

@ConfigSerializable
data class CommandMessages(
    val help: CommandHelpMessages = CommandHelpMessages(),
    val reload: CommandReloadMessages = CommandReloadMessages(),
    override val exception: CommandExceptionMessages = CommandExceptionMessages(),
) : AbstractCommandMessages

@ConfigSerializable
data class CommandHelpMessages(
    val title: String = "<prefix> <#0EA5E9>SimpleCloud Prefixes commands",
    val entry: String = "<#E2E8F0><command>",
)

@ConfigSerializable
data class CommandReloadMessages(
    val success: String = "<prefix> <#A3E635>SimpleCloud Prefixes was successfully reloaded.",
    val failed: String = "<prefix> <#DC2626>SimpleCloud Prefixes could not be reloaded.",
)
