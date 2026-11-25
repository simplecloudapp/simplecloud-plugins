package app.simplecloud.plugin.command.shared.config

import app.simplecloud.plugin.command.shared.utilities.ConfigVersion
import app.simplecloud.core.shared.command.HelpMessages
import app.simplecloud.core.shared.command.PageMessages
import app.simplecloud.core.shared.config.VersionedConfig
import app.simplecloud.core.shared.config.command.AbstractCommandMessageConfig
import app.simplecloud.core.shared.config.command.AbstractCommandMessages
import app.simplecloud.core.shared.config.command.CommandExceptionMessages
import app.simplecloud.core.shared.config.message.MessagePalette
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MessageConfig(
    override val version: Int = ConfigVersion.VERSION,
    val palette: Map<String, String> = MessagePalette.defaults,
    val variables: Map<String, String> = mapOf("prefix" to MessagePalette.PREFIX),
    override val command: CommandSection = CommandSection(),
) : VersionedConfig, AbstractCommandMessageConfig(palette, variables)

@ConfigSerializable
data class CommandSection(
    val help: HelpSection = HelpSection(),
    val usage: UsageSection = UsageSection(),
    val page: PageSection = PageSection(),
    val group: GroupSection = GroupSection(),
    val server: ServerSection = ServerSection(),
    val player: PlayerSection = PlayerSection(),
    val persistentServer: PersistentServerSection = PersistentServerSection(),
    val info: InfoSection = InfoSection(),
    val reload: ReloadSection = ReloadSection(),
    override val exception: CommandExceptionMessages = CommandExceptionMessages(),
) : AbstractCommandMessages

@ConfigSerializable
data class HelpSection(
    val title: String = "<prefix> <info>Cloud commands  <separator>/cloud",
    override val entry: String = HelpMessages.ENTRY,
    override val hover: String = HelpMessages.HOVER,
    val empty: String = "<prefix> <warning>You can't use any cloud commands.",
    override val descriptions: Map<String, String> = mapOf(
        "group list" to "List all groups",
        "group info <group>" to "Show a group",
        "group start <group> [count]" to "Start servers of a group",
        "group stop <group> [ids]" to "Stop servers of a group",
        "group rollout <group>" to "Restart a group server by server",
        "group rollout <group> status" to "Show the rollout of a group",
        "group rollout <group> cancel" to "Cancel the rollout of a group",
        "group queue <group>" to "Show the start queue of a group",
        "group queue <group> clear" to "Clear the start queue of a group",
        "group enable <group>" to "Enable a group so it starts servers",
        "group disable <group>" to "Disable a group and stop its servers",
        "server list [group] [page]" to "List the running servers",
        "server info <server>" to "Show a server",
        "server start <group>" to "Start a new server",
        "server stop <server>" to "Stop a server",
        "server command <server> <command>" to "Run a console command on a server",
        "ps list" to "List all persistent servers",
        "ps info <name>" to "Show a persistent server",
        "ps enable <name>" to "Enable and start a persistent server",
        "ps disable <name>" to "Disable and stop a persistent server",
        "player list [<group|ps|server> <target>] [page]" to "List the online players",
        "player info <player>" to "Show a player",
        "player send <player> <group|ps|server> <target> [mode]" to "Send a player to a target",
        "player sendall <group|ps|server> <target> [mode]" to "Send all players to a target",
        "player sendfrom <source> <group|ps|server> <target> [mode]" to "Move the players of a source",
        "player message <player> <message>" to "Send a player a message",
        "player kick <player> [reason]" to "Kick a player from the network",
        "info" to "Show the network stats",
        "reload" to "Reload the messages",
    ),
) : HelpMessages

@ConfigSerializable
data class PageSection(
    override val footer: String = PageMessages.FOOTER,
    override val previous: String = PageMessages.PREVIOUS,
    override val next: String = PageMessages.NEXT,
) : PageMessages

@ConfigSerializable
data class UsageSection(
    val invalidTargetType: String = "<prefix> <error>Use group, ps or server as the target type.",
    val modeOnlyForGroups: String = "<prefix> <error>A selection mode only works with a group as the target.",
)

@ConfigSerializable
data class GroupSection(
    val list: GroupListSection = GroupListSection(),
    val info: GroupInfoSection = GroupInfoSection(),
    val start: GroupStartSection = GroupStartSection(),
    val stop: GroupStopSection = GroupStopSection(),
    val rollout: GroupRolloutSection = GroupRolloutSection(),
    val queue: GroupQueueSection = GroupQueueSection(),
    val activation: GroupActivationSection = GroupActivationSection(),
    val error: GroupErrorSection = GroupErrorSection(),
)

@ConfigSerializable
data class GroupListSection(
    val title: String = "<prefix> <info>Groups <separator>(</separator><muted><count></muted><separator>)</separator>",
    val entry: String = "<separator>•</separator> <highlight><group> <separator>(</separator><muted><online>/<total> online</muted><separator>)</separator>",
    val empty: String = "<prefix> <warning>No groups were found.",
)

@ConfigSerializable
data class GroupInfoSection(
    val title: String = "<prefix> <info>Group <group>",
    val entry: String = "<separator>•</separator> <muted><key><separator>:</separator> <highlight><value>",
)

@ConfigSerializable
data class GroupStartSection(
    val success: String = "<prefix> <info>Sent the start request for <count> servers of group <group>.",
    val partial: String = "<prefix> <warning>Started only <count>/<total> servers for group <group> because <reason>.",
)

@ConfigSerializable
data class GroupStopSection(
    val success: String = "<prefix> <info>Sent the stop request for <count> servers of group <group>.",
    val successWithIds: String = "<prefix> <info>Sent the stop request for servers <ids> of group <group>.",
    val partial: String = "<prefix> <warning>Stopped only <count>/<total> servers of group <group> because <reason>.",
)

@ConfigSerializable
data class GroupRolloutSection(
    val started: String = "<prefix> <success>Started the rollout of group <group>.",
    val cancelled: String = "<prefix> <success>Cancelled the rollout of group <group>.",
    val status: GroupRolloutStatusSection = GroupRolloutStatusSection(),
    val error: GroupRolloutErrorSection = GroupRolloutErrorSection(),
)

@ConfigSerializable
data class GroupRolloutStatusSection(
    val title: String = "<prefix> <info>Rollout of group <group>",
    val entry: String = "<separator>•</separator> <muted><key><separator>:</separator> <highlight><value>",
    val inactive: String = "<prefix> <warning>Group <group> has no active rollout.",
)

@ConfigSerializable
data class GroupRolloutErrorSection(
    val alreadyActive: String = "<prefix> <warning>Group <group> already has an active rollout.",
    val noServers: String = "<prefix> <warning>Group <group> has no running servers to roll out.",
    val notActive: String = "<prefix> <warning>Group <group> has no active rollout to cancel.",
)

@ConfigSerializable
data class GroupActivationSection(
    val enabled: String = "<prefix> <success>Enabled group <group>, it starts servers again.",
    val disabled: String = "<prefix> <success>Disabled group <group>, its servers are stopping.",
    val alreadyEnabled: String = "<prefix> <warning>Group <group> is already enabled.",
    val alreadyDisabled: String = "<prefix> <warning>Group <group> is already disabled.",
)

@ConfigSerializable
data class GroupQueueSection(
    val title: String = "<prefix> <info>Start queue of group <group> <separator>(</separator><muted><count></muted><separator>)</separator>",
    val entry: String = "<separator>•</separator> <muted><key><separator>:</separator> <highlight><value>",
    val pending: String = "<separator>•</separator> <muted>Pending since <highlight><date>",
    val failed: String = "<separator>•</separator> <muted>Failed at <highlight><date></highlight> <separator>(</separator><error><reason></error><separator>)</separator>",
    val more: String = "<separator>•</separator> <muted>... and <count> more",
    val empty: String = "<prefix> <warning>The start queue of group <group> is empty.",
    val cleared: String = "<prefix> <success>Cleared <count> queued starts of group <group>.",
    val alreadyEmpty: String = "<prefix> <warning>The start queue of group <group> is already empty.",
)

@ConfigSerializable
data class GroupErrorSection(
    val notFound: String = "<prefix> <error>Group <group> was not found.",
    val alreadyEmpty: String = "<prefix> <warning>Group <group> has no running servers.",
    val startFailed: String = "<prefix> <error>Couldn't start servers for group <group> because <reason>.",
    val stopFailed: String = "<prefix> <error>Couldn't stop the servers of group <group> because <reason>.",
    val limitReached: String = "<prefix> <warning>Group <group> can only start <available> more servers <separator>(</separator><muted><running>/<max> running or queued</muted><separator>)</separator>.",
    val invalidIds: String = "<prefix> <error><ids> are not valid server ids.",
    val serversNotFound: String = "<prefix> <error>Servers <ids> of group <group> were not found.",
    val alreadyStopping: String = "<prefix> <warning>Servers <ids> of group <group> are already stopping.",
)

@ConfigSerializable
data class ServerSection(
    val list: ServerListSection = ServerListSection(),
    val info: ServerInfoSection = ServerInfoSection(),
    val start: ServerStartSection = ServerStartSection(),
    val stop: ServerStopSection = ServerStopSection(),
    val command: ServerCommandSection = ServerCommandSection(),
    val error: ServerErrorSection = ServerErrorSection(),
)

@ConfigSerializable
data class ServerListSection(
    val title: String = "<prefix> <info>Servers <separator>(</separator><muted><count></muted><separator>)</separator>",
    val entry: String = "<separator>•</separator> <highlight><server> <separator>(</separator><muted><value></muted><separator>)</separator>",
    val empty: String = "<prefix> <warning>No running servers were found.",
)

@ConfigSerializable
data class ServerInfoSection(
    val title: String = "<prefix> <info>Server <server>",
    val entry: String = "<separator>•</separator> <muted><key><separator>:</separator> <highlight><value>",
)

@ConfigSerializable
data class ServerStartSection(
    val requested: String = "<prefix> <info>Sent the start request for a new server of group <group>.",
)

@ConfigSerializable
data class ServerStopSection(
    val success: String = "<prefix> <info>Sent the stop request for server <server>.",
)

@ConfigSerializable
data class ServerCommandSection(
    val success: String = "<prefix> <success>Sent <highlight><command></highlight> to server <server>.",
)

@ConfigSerializable
data class ServerErrorSection(
    val notFound: String = "<prefix> <error>Server <server> was not found.",
    val alreadyStopping: String = "<prefix> <warning>Server <server> is already stopping.",
    val commandEmpty: String = "<prefix> <error>Write a command for server <server>.",
)

@ConfigSerializable
data class PersistentServerSection(
    val list: PersistentServerListSection = PersistentServerListSection(),
    val info: PersistentServerInfoSection = PersistentServerInfoSection(),
    val activation: PersistentServerActivationSection = PersistentServerActivationSection(),
    val error: PersistentServerErrorSection = PersistentServerErrorSection(),
)

@ConfigSerializable
data class PersistentServerListSection(
    val title: String = "<prefix> <info>Persistent servers <separator>(</separator><muted><count></muted><separator>)</separator>",
    val entry: String = "<separator>•</separator> <highlight><server> <separator>(</separator><muted><value></muted><separator>)</separator>",
    val empty: String = "<prefix> <warning>No persistent servers were found.",
)

@ConfigSerializable
data class PersistentServerInfoSection(
    val title: String = "<prefix> <info>Persistent server <server>",
    val entry: String = "<separator>•</separator> <muted><key><separator>:</separator> <highlight><value>",
)

@ConfigSerializable
data class PersistentServerActivationSection(
    val enabled: String = "<prefix> <success>Enabled persistent server <server>, it is starting.",
    val disabled: String = "<prefix> <success>Disabled persistent server <server>, it is stopping.",
    val alreadyEnabled: String = "<prefix> <warning>Persistent server <server> is already enabled.",
    val alreadyDisabled: String = "<prefix> <warning>Persistent server <server> is already disabled.",
)

@ConfigSerializable
data class PersistentServerErrorSection(
    val notFound: String = "<prefix> <error>Persistent server <server> was not found.",
)

@ConfigSerializable
data class PlayerSection(
    val list: PlayerListSection = PlayerListSection(),
    val info: PlayerInfoSection = PlayerInfoSection(),
    val send: PlayerSendSection = PlayerSendSection(),
    val message: PlayerMessageSection = PlayerMessageSection(),
    val kick: PlayerKickSection = PlayerKickSection(),
    val error: PlayerErrorSection = PlayerErrorSection(),
)

@ConfigSerializable
data class PlayerListSection(
    val title: String = "<prefix> <info>Players <separator>(</separator><muted><count></muted><separator>)</separator>",
    val entry: String = "<separator>•</separator> <highlight><white><head:<playername>:true></white> <displayname> <separator>(</separator><muted><value></muted><separator>)</separator>",
    val empty: String = "<prefix> <warning>No players were found.",
)

@ConfigSerializable
data class PlayerInfoSection(
    val title: String = "<prefix> <info>Player <white><head:<playername>:true></white> <displayname>",
    val entry: String = "<separator>•</separator> <muted><key><separator>:</separator> <highlight><value>",
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
data class PlayerKickSection(
    val success: String = "<prefix> <success>Kicked <white><head:<playername>:true></white> <displayname>.",
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
)

@ConfigSerializable
data class InfoSection(
    val title: String = "<prefix> <info>Cloud network",
    val entry: String = "<separator>•</separator> <muted><key><separator>:</separator> <highlight><value>",
)

@ConfigSerializable
data class ReloadSection(
    val success: String = "<prefix> <success>Reloaded the cloud commands!",
    val failed: String = "<prefix> <error>Couldn't reload the cloud commands. Check the console.",
)
