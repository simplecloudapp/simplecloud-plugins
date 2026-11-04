package app.simplecloud.plugin.proxy.shared.config

import app.simplecloud.plugin.core.shared.command.HelpMessages
import app.simplecloud.plugin.core.shared.config.AbstractCommandMessageConfig
import app.simplecloud.plugin.core.shared.config.AbstractCommandMessages
import app.simplecloud.plugin.core.shared.config.CommandExceptionMessages
import app.simplecloud.plugin.core.shared.config.MessagePalette
import app.simplecloud.plugin.core.shared.config.VersionedConfig
import app.simplecloud.plugin.proxy.shared.utilities.ConfigVersion
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MessageConfig(
    override val version: Int = ConfigVersion.VERSION,
    override val palette: Map<String, String> = MessagePalette.defaults,
    override val variables: Map<String, String> = mapOf(
        "prefix" to MessagePalette.PREFIX,
    ),
    val format: FormatConfig = FormatConfig(),
    val kick: KickMessages = KickMessages(),
    override val command: CommandMessages = CommandMessages(),
) : VersionedConfig, AbstractCommandMessageConfig()

@ConfigSerializable
data class FormatConfig(
    val date: String = "dd.MM.yyyy",
    val time: String = "HH:mm:ss",
    val pingColors: List<PingColor> = listOf(
        PingColor(0, "<success>"),
        PingColor(100, "<warning>"),
        PingColor(200, "<error>"),
    ),
)

@ConfigSerializable
data class PingColor(
    val ping: Int = 0,
    val color: String = "",
)

@ConfigSerializable
data class KickMessages(
    val noPermission: String = "<warning>The network is in maintenance.<br><br><muted>Try again later.",
    val networkFull: String = "<error>The network is full.<br><br><muted>Try again in a few minutes.",
    val noJoinState: String = "<error>This server can't be joined right now.<br><br><muted>Try again later.",
)

@ConfigSerializable
data class CommandMessages(
    val help: HelpListMessages = HelpListMessages(
        header = "<prefix> <info>Proxy commands  <separator>/scproxy",
        descriptions = mapOf(
            "reload" to "Reload the configuration",
            "joinstate list" to "List the join states of all groups",
            "joinstate info" to "Show the join state of this server",
            "joinstate info <group|ps> <target> [id]" to "Show the join state of a target",
            "joinstate set <group|ps> <target> <joinstate> [id]" to "Change the join state of a target",
            "layout info [group]" to "Show the layout of a group",
            "layout set <group> <layout>" to "Change the layout of a group",
        ),
    ),
    val failure: String = "<prefix> <error>The command failed. Check the console.",
    val invalidTargetType: String = "<prefix> <error>Use group or ps as the target type.",
    val persistentServerId: String = "<prefix> <error>Persistent servers don't have an id.",
    val targetNotFound: String = "<prefix> <error>Target <target> was not found.",
    val reload: ReloadMessages = ReloadMessages(),
    val joinState: JoinStateMessages = JoinStateMessages(),
    val layout: LayoutMessages = LayoutMessages(),
    override val exception: CommandExceptionMessages = CommandExceptionMessages(),
) : AbstractCommandMessages

@ConfigSerializable
data class ReloadMessages(
    val start: String = "<prefix> <info>Reloading the proxy configuration.",
    val success: String = "<prefix> <success>Reloaded the proxy configuration!",
    val failure: String = "<prefix> <error>Couldn't reload the proxy configuration because <reason>.",
)

@ConfigSerializable
data class JoinStateMessages(
    val server: UpdateMessages = UpdateMessages(
        updateSuccess = "<prefix> <success>Updated the join state of the server.",
        updateFailure = "<prefix> <error>Couldn't update the join state of the server.",
        updateNoChange = "<prefix> <warning>The server already has this join state.",
    ),
    val group: UpdateMessages = UpdateMessages(
        updateSuccess = "<prefix> <success>Updated the join state of the group.",
        updateFailure = "<prefix> <error>Couldn't update the join state of the group.",
        updateNoChange = "<prefix> <warning>The group already has this join state.",
    ),
    val help: HelpListMessages = HelpListMessages(
        header = "<prefix> <info>Join state commands  <separator>/scproxy",
        descriptions = mapOf(
            "joinstate list" to "List the join states of all groups",
            "joinstate info" to "Show the join state of this server",
            "joinstate info <group|ps> <target> [id]" to "Show the join state of a target",
            "joinstate set <group|ps> <target> <joinstate> [id]" to "Change the join state of a target",
        ),
    ),
    val list: JoinStateListMessages = JoinStateListMessages(),
)

@ConfigSerializable
data class JoinStateListMessages(
    val groups: ListMessages = ListMessages(
        header = "<prefix> <info>Groups and their join states",
        entry = "<separator>•</separator> <info><group> <separator>(</separator><state><separator>)</separator>",
    ),
    val states: ListMessages = ListMessages(
        header = "<prefix> <info>Join states",
        entry = "<separator>•</separator> <info><state> <separator>(</separator><join_permission><separator>)</separator>",
    ),
)

@ConfigSerializable
data class LayoutMessages(
    val help: HelpListMessages = HelpListMessages(
        header = "<prefix> <info>Layout commands  <separator>/scproxy",
        descriptions = mapOf(
            "layout info [group]" to "Show the layout of a group",
            "layout set <group> <layout>" to "Change the layout of a group",
        ),
    ),
    val info: ListMessages = ListMessages(
        header = "<prefix> <info>Layout of group <group>",
        entry = "<separator>•</separator> <info><layout>",
    ),
    val set: UpdateMessages = UpdateMessages(
        updateSuccess = "<prefix> <success>Updated the layout of the group.",
        updateFailure = "<prefix> <error>Couldn't update the layout of the group.",
        updateNoChange = "<prefix> <warning>The group already has this layout.",
    ),
)

@ConfigSerializable
data class ListMessages(
    val header: String = "",
    val entry: String = "",
)

@ConfigSerializable
data class HelpListMessages(
    val header: String = "",
    override val entry: String = HelpMessages.ENTRY,
    override val hover: String = HelpMessages.HOVER,
    override val descriptions: Map<String, String> = emptyMap(),
) : HelpMessages

@ConfigSerializable
data class UpdateMessages(
    val updateSuccess: String = "<prefix> <success>Updated!",
    val updateFailure: String = "<prefix> <error>Couldn't update it.",
    val updateNoChange: String = "<prefix> <warning>Nothing changed.",
)
