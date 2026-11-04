package app.simplecloud.plugin.command.shared.config

import app.simplecloud.plugin.command.shared.utilities.ConfigVersion
import app.simplecloud.plugin.core.shared.config.AbstractCommandMessageConfig
import app.simplecloud.plugin.core.shared.config.AbstractCommandMessages
import app.simplecloud.plugin.core.shared.config.CommandExceptionMessages
import app.simplecloud.plugin.core.shared.config.MessagePalette
import app.simplecloud.plugin.core.shared.config.VersionedConfig
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MessageConfig(
    override val version: Int = ConfigVersion.VERSION,
    override val palette: Map<String, String> = MessagePalette.defaults,
    override val variables: Map<String, String> = mapOf("prefix" to MessagePalette.PREFIX),
    override val command: CommandSection = CommandSection(),
) : VersionedConfig, AbstractCommandMessageConfig()

@ConfigSerializable
data class CommandSection(
    val help: HelpSection = HelpSection(),
    val usage: UsageSection = UsageSection(),
    val group: GroupSection = GroupSection(),
    val server: ServerSection = ServerSection(),
    val player: PlayerSection = PlayerSection(),
    val reload: ReloadSection = ReloadSection(),
    override val exception: CommandExceptionMessages = CommandExceptionMessages(),
) : AbstractCommandMessages

@ConfigSerializable
data class HelpSection(
    val title: String = "<prefix> <info>Cloud commands",
    val entry: String = "<separator>•</separator> <interactive><command></interactive> <muted><arguments>",
    val hover: String = "<interactive>Click to type this command",
    val empty: String = "<prefix> <warning>You can't use any cloud commands.",
)

@ConfigSerializable
data class UsageSection(
    val invalidTargetType: String = "<prefix> <error>Use group or ps as the target type.",
)

@ConfigSerializable
data class GroupSection(
    val list: GroupListSection = GroupListSection(),
    val info: GroupInfoSection = GroupInfoSection(),
    val start: GroupStartSection = GroupStartSection(),
    val stop: GroupStopSection = GroupStopSection(),
    val error: GroupErrorSection = GroupErrorSection(),
)

@ConfigSerializable
data class GroupListSection(
    val title: String = "<prefix> <info>Groups <separator>(</separator><count><separator>)</separator>",
    val entry: String = "<separator>•</separator> <info><group> <separator>(</separator><online>/<total> online<separator>)</separator>",
    val empty: String = "<prefix> <warning>No groups were found.",
)

@ConfigSerializable
data class GroupInfoSection(
    val title: String = "<prefix> <info>Group <group>",
    val entry: String = "<separator>•</separator> <info><key><separator>:</separator> <value>",
)

@ConfigSerializable
data class GroupStartSection(
    val success: String = "<prefix> <info>Sent the start request for <count> servers of group <group>.",
    val partial: String = "<prefix> <warning>Started only <count>/<total> servers for group <group> because <reason>.",
)

@ConfigSerializable
data class GroupStopSection(
    val success: String = "<prefix> <info>Sent the stop request for the servers of group <group>.",
    val successWithIds: String = "<prefix> <info>Sent the stop request for servers <ids> of group <group>.",
    val partial: String = "<prefix> <warning>Stopped only <count>/<total> servers of group <group> because <reason>.",
)

@ConfigSerializable
data class GroupErrorSection(
    val notFound: String = "<prefix> <error>Group <group> was not found.",
    val alreadyEmpty: String = "<prefix> <warning>Group <group> has no running servers.",
    val startFailed: String = "<prefix> <error>Couldn't start servers for group <group> because <reason>.",
    val stopFailed: String = "<prefix> <error>Couldn't stop the servers of group <group> because <reason>.",
)

@ConfigSerializable
data class ServerSection(
    val list: ServerListSection = ServerListSection(),
    val info: ServerInfoSection = ServerInfoSection(),
    val start: ServerStartSection = ServerStartSection(),
    val stop: ServerStopSection = ServerStopSection(),
    val error: ServerErrorSection = ServerErrorSection(),
)

@ConfigSerializable
data class ServerListSection(
    val title: String = "<prefix> <info>Servers <separator>(</separator><count><separator>)</separator>",
    val entry: String = "<separator>•</separator> <info><server> <separator>(</separator><value><separator>)</separator>",
    val empty: String = "<prefix> <warning>No running servers were found.",
)

@ConfigSerializable
data class ServerInfoSection(
    val title: String = "<prefix> <info>Server <server>",
    val entry: String = "<separator>•</separator> <info><key><separator>:</separator> <value>",
)

@ConfigSerializable
data class ServerStartSection(
    val requested: String = "<prefix> <info>Sent the start request for a new server of group <group>.",
)

@ConfigSerializable
data class ServerStopSection(
    val success: String = "<prefix> <info>Sent the stop request for server <group>-<id>.",
)

@ConfigSerializable
data class ServerErrorSection(
    val notFound: String = "<prefix> <error>Server <group>-<id> was not found.",
    val alreadyStopping: String = "<prefix> <warning>Server <group>-<id> is already stopping.",
    val startFailed: String = "<prefix> <error>Couldn't queue a new server for group <group> because <reason>.",
    val stopFailed: String = "<prefix> <error>Couldn't stop server <group>-<id> because <reason>.",
)

@ConfigSerializable
data class PlayerSection(
    val list: PlayerListSection = PlayerListSection(),
    val info: PlayerInfoSection = PlayerInfoSection(),
    val send: PlayerSendSection = PlayerSendSection(),
    val message: PlayerMessageSection = PlayerMessageSection(),
    val error: PlayerErrorSection = PlayerErrorSection(),
)

@ConfigSerializable
data class PlayerListSection(
    val title: String = "<prefix> <info>Players <separator>(</separator><count><separator>)</separator>",
    val entry: String = "<separator>•</separator> <info><white><head:<playername>:true></white> <displayname> <separator>(</separator><value><separator>)</separator>",
    val empty: String = "<prefix> <warning>No players were found on this target.",
)

@ConfigSerializable
data class PlayerInfoSection(
    val title: String = "<prefix> <info>Player <white><head:<playername>:true></white> <displayname>",
    val entry: String = "<separator>•</separator> <info><key><separator>:</separator> <value>",
)

@ConfigSerializable
data class PlayerSendSection(
    val success: String = "<prefix> <success>Sent <white><head:<playername>:true></white> <displayname> to <target>.",
    val successAll: String = "<prefix> <success>Sent <count> players to <target>.",
    val successFromServer: String = "<prefix> <success>Sent <count> players from <source> to <target>.",
)

@ConfigSerializable
data class PlayerMessageSection(
    val success: String = "<prefix> <success>Sent your message to <white><head:<playername>:true></white> <displayname>.",
)

@ConfigSerializable
data class PlayerErrorSection(
    val notFound: String = "<prefix> <error>Player <playername> was not found.",
    val notOnline: String = "<prefix> <error>Player <playername> is not online.",
    val sameTarget: String = "<prefix> <warning><white><head:<playername>:true></white> <displayname> is already on <target>.",
    val targetNotFound: String = "<prefix> <error>Target <target> was not found.",
    val sourceNotFound: String = "<prefix> <error>Source <source> was not found.",
    val sendFailed: String = "<prefix> <error>Couldn't send <white><head:<playername>:true></white> <displayname> to <target>.",
    val sendAllFailed: String = "<prefix> <error>Couldn't send the players to <target>.",
    val noOnlinePlayers: String = "<prefix> <warning>No players are online.",
    val allOnTarget: String = "<prefix> <warning>All players are already on <target>.",
    val noSourcePlayers: String = "<prefix> <warning>No players are on <source>.",
    val messageEmpty: String = "<prefix> <error>Write a message for <playername>.",
    val messageFailed: String = "<prefix> <error>Couldn't send the message to <playername>.",
)

@ConfigSerializable
data class ReloadSection(
    val success: String = "<prefix> <success>Reloaded the cloud commands!",
    val failed: String = "<prefix> <error>Couldn't reload the cloud commands. Check the console.",
)
