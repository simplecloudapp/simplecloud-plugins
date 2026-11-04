package app.simplecloud.plugin.signs.shared.config

import app.simplecloud.plugin.core.shared.config.AbstractCommandMessageConfig
import app.simplecloud.plugin.core.shared.config.AbstractCommandMessages
import app.simplecloud.plugin.core.shared.config.CommandExceptionMessages
import app.simplecloud.plugin.core.shared.config.MessagePalette
import app.simplecloud.plugin.core.shared.config.VersionedConfig
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MessageConfig(
    override val version: Int = VERSION,
    override val palette: Map<String, String> = MessagePalette.defaults,
    override val variables: Map<String, String> = mapOf("prefix" to "<color:#38bdf8><bold>⚡</bold></color>"),
    override val command: CommandMessages = CommandMessages(),
) : VersionedConfig, AbstractCommandMessageConfig() {

    companion object {
        const val VERSION = 1
    }
}

@ConfigSerializable
data class CommandMessages(
    val help: CommandHelpMessages = CommandHelpMessages(),
    val add: CommandAddMessages = CommandAddMessages(),
    val remove: CommandRemoveMessages = CommandRemoveMessages(),
    val list: CommandListMessages = CommandListMessages(),
    val notLookingAtSign: String = "<prefix> <#DC2626>To execute this command, you need to look at a sign.",
    override val exception: CommandExceptionMessages = CommandExceptionMessages(),
) : AbstractCommandMessages

@ConfigSerializable
data class CommandHelpMessages(
    val title: String = "<prefix> <#FFFFFF>Commands of the SimpleCloud Signs plugin",
    val entry: String = "   <#A3A3A3><command>",
)

@ConfigSerializable
data class CommandAddMessages(
    val success: String = "<prefix> <#FFFFFF>Sign for <#FBBF24><target></#FBBF24> was <#A3E635>created</#A3E635>.",
    val alreadyRegistered: String = "<prefix> <#DC2626>This sign is already registered.",
    val groupNotFound: String = "<prefix> <#DC2626>There is no group named <#FBBF24><group></#FBBF24>.",
    val persistentServerNotFound: String =
        "<prefix> <#DC2626>There is no persistent server named <#FBBF24><persistent-server></#FBBF24>.",
)

@ConfigSerializable
data class CommandRemoveMessages(
    val success: String = "<prefix> <#A3E635>Sign was successfully removed.",
    val notRegistered: String = "<prefix> <#DC2626>This sign is not registered.",
    val allSuccess: String =
        "<prefix> <#FFFFFF>Successfully removed <#FBBF24><amount></#FBBF24> sign(s) of <#FBBF24><target></#FBBF24>.",
    val noneRegistered: String = "<prefix> <#DC2626>No signs are registered for <#FBBF24><target></#FBBF24>.",
)

@ConfigSerializable
data class CommandListMessages(
    val empty: String = "<prefix> <#DC2626>There are no signs registered.",
    val title: String = "<prefix> <#FFFFFF>Registered signs <#A3A3A3>(<amount>)",
    val entry: String =
        "<teleport><#A3A3A3>└─ <#FBBF24><target></#FBBF24> in <#FFFFFF><world></#FFFFFF> at <#FFFFFF><x> <y> <z></teleport>",
    val teleportHover: String = "<#A3A3A3>Click to teleport",
    val footer: String = "<previous><#A3A3A3>[ <#FFFFFF><page>/<pages></#FFFFFF> ]<next>",
    val previous: String = "<#38BDF8>« Previous </#38BDF8>",
    val next: String = "<#38BDF8> Next »</#38BDF8>",
)
