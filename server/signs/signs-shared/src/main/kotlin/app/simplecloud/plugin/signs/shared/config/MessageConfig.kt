package app.simplecloud.plugin.signs.shared.config

import app.simplecloud.plugin.core.shared.command.HelpMessages
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
    override val variables: Map<String, String> = mapOf("prefix" to MessagePalette.PREFIX),
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
    val notLookingAtSign: String = "<prefix> <error>Look at a sign to use this command.",
    override val exception: CommandExceptionMessages = CommandExceptionMessages(),
) : AbstractCommandMessages

@ConfigSerializable
data class CommandHelpMessages(
    val title: String = "<prefix> <info>Sign commands  <separator>/scsigns",
    override val entry: String = HelpMessages.ENTRY,
    override val hover: String = HelpMessages.HOVER,
    override val descriptions: Map<String, String> = mapOf(
        "add group <group>" to "Turn this sign into a group sign",
        "add persistent <name>" to "Turn this sign into a server sign",
        "remove [group|persistent server]" to "Remove this sign or all of a target",
        "list [page] [group|persistent server]" to "List the registered signs",
    ),
) : HelpMessages

@ConfigSerializable
data class CommandAddMessages(
    val success: String = "<prefix> <success>Created a sign for <target>!",
    val alreadyRegistered: String = "<prefix> <warning>This sign is already registered.",
    val groupNotFound: String = "<prefix> <error>There is no group named <group>.",
    val persistentServerNotFound: String =
        "<prefix> <error>There is no persistent server named <persistent-server>.",
)

@ConfigSerializable
data class CommandRemoveMessages(
    val success: String = "<prefix> <success>Removed the sign.",
    val notRegistered: String = "<prefix> <warning>This sign is not registered.",
    val allSuccess: String =
        "<prefix> <success>Removed the signs of <target> <separator>(</separator><amount><separator>)</separator>.",
    val noneRegistered: String = "<prefix> <warning>No signs are registered for <target>.",
)

@ConfigSerializable
data class CommandListMessages(
    val empty: String = "<prefix> <warning>No signs are registered.",
    val title: String = "<prefix> <info>Signs <separator>(</separator><amount><separator>)</separator>",
    val entry: String =
        "<teleport><separator>•</separator> <interactive><target> in <world> at <x> <y> <z></teleport>",
    val teleportHover: String = "<interactive>Click to teleport",
    val footer: String = "<previous><info>Page <page>/<pages><next>",
    val previous: String = "<interactive>Previous</interactive>  ",
    val next: String = "  <interactive>Next",
)
