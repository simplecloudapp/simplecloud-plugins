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
    override val variables: Map<String, String> = mapOf("prefix" to MessagePalette.PREFIX),
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
    val title: String = "<prefix> <info>Prefixes commands",
    val entry: String = "<separator>•</separator> <interactive><command>",
    val hover: String = "<interactive>Click to type this command",
)

@ConfigSerializable
data class CommandReloadMessages(
    val success: String = "<prefix> <success>Reloaded the prefixes!",
    val failed: String = "<prefix> <error>Couldn't reload the prefixes. Check the console.",
)
