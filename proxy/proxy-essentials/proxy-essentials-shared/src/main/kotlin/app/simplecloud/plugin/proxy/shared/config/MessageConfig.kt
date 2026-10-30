package app.simplecloud.plugin.proxy.shared.config

import app.simplecloud.plugin.core.shared.config.AbstractCommandMessageConfig
import app.simplecloud.plugin.core.shared.config.AbstractCommandMessages
import app.simplecloud.plugin.core.shared.config.CommandExceptionMessages
import app.simplecloud.plugin.core.shared.config.VersionedConfig
import app.simplecloud.plugin.proxy.shared.utilities.ConfigVersion
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MessageConfig(
    override val version: Int = ConfigVersion.VERSION,
    override val variables: Map<String, String> = mapOf(
        "prefix" to "<color:#38bdf8><bold>⚡</bold></color>",
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
        PingColor(0, "<green>"),
        PingColor(50, "<yellow>"),
        PingColor(100, "<gold>"),
        PingColor(150, "<red>"),
        PingColor(200, "<dark_red>"),
    ),
)

@ConfigSerializable
data class PingColor(
    val ping: Int = 0,
    val color: String = "",
)

@ConfigSerializable
data class KickMessages(
    val noPermission: String = "<#DC2626>The network is currently in maintenance mode. Please try again later.",
    val networkFull: String = "<#DC2626>The network is currently full. Please try again later.",
    val noJoinState: String = "<#DC2626>No join state found for server. Please try again later.",
)

@ConfigSerializable
data class CommandMessages(
    val help: ListMessages = ListMessages(
        header = "<prefix> <#0EA5E9>SimpleCloud ProxyEssentials commands",
        entry = "<#E2E8F0><command>",
    ),
    val failure: String = "<prefix> <#DC2626>The command failed, check the console for details.",
    val invalidTargetType: String = "<prefix> <#DC2626>Use <#F8FAFC>group <#DC2626>or <#F8FAFC>ps <#DC2626>as target type.",
    val persistentServerId: String = "<prefix> <#DC2626>Persistent servers do not have an id.",
    val targetNotFound: String = "<prefix> <#DC2626>Target <#F8FAFC><target> <#DC2626>was not found.",
    val reload: ReloadMessages = ReloadMessages(),
    val joinState: JoinStateMessages = JoinStateMessages(),
    val layout: LayoutMessages = LayoutMessages(),
    override val exception: CommandExceptionMessages = CommandExceptionMessages(),
) : AbstractCommandMessages()

@ConfigSerializable
data class ReloadMessages(
    val start: String = "<prefix> <#0EA5E9>Reloading ProxyEssentials configurations...",
    val success: String = "<prefix> <#A3E635>ProxyEssentials configurations were reloaded.",
    val failure: String = "<prefix> <#DC2626>ProxyEssentials configurations could not be reloaded: <#F8FAFC><error>",
)

@ConfigSerializable
data class JoinStateMessages(
    val server: UpdateMessages = UpdateMessages(
        updateSuccess = "<prefix> <#A3E635>Join state of server was updated.",
        updateFailure = "<prefix> <#DC2626>Join state of server could not be updated.",
        updateNoChange = "<prefix> <#F59E0B>Join state of server did not change.",
    ),
    val group: UpdateMessages = UpdateMessages(
        updateSuccess = "<prefix> <#A3E635>Join state of group was updated.",
        updateFailure = "<prefix> <#DC2626>Join state of group could not be updated.",
        updateNoChange = "<prefix> <#F59E0B>Join state of group did not change.",
    ),
    val help: ListMessages = ListMessages(
        header = "<prefix> <#0EA5E9>Join state commands",
        entry = "<#E2E8F0><command>",
    ),
    val list: JoinStateListMessages = JoinStateListMessages(),
)

@ConfigSerializable
data class JoinStateListMessages(
    val groups: ListMessages = ListMessages(
        header = "<prefix> <#0EA5E9>Groups with their join states",
        entry = "   <#475569>- <#F8FAFC><group> <#475569>(<#E2E8F0><state><#475569>)",
    ),
    val states: ListMessages = ListMessages(
        header = "<prefix> <#0EA5E9>Available join states",
        entry = "   <#475569>- <#F8FAFC><state> <#475569>(<#E2E8F0><join_permission><#475569>)",
    ),
)

@ConfigSerializable
data class LayoutMessages(
    val help: ListMessages = ListMessages(
        header = "<prefix> <#0EA5E9>Layout commands",
        entry = "<#E2E8F0><command>",
    ),
    val info: ListMessages = ListMessages(
        header = "<prefix> <#0EA5E9>Layout of group <#F8FAFC><group>",
        entry = "   <#475569>- <#F8FAFC><layout>",
    ),
    val set: UpdateMessages = UpdateMessages(
        updateSuccess = "<prefix> <#A3E635>Layout of group was updated.",
        updateFailure = "<prefix> <#DC2626>Layout of group could not be updated.",
        updateNoChange = "<prefix> <#F59E0B>Layout of group did not change.",
    ),
)

@ConfigSerializable
data class ListMessages(
    val header: String = "",
    val entry: String = "",
)

@ConfigSerializable
data class UpdateMessages(
    val updateSuccess: String = "<prefix> <#A3E635>Updated successfully.",
    val updateFailure: String = "<prefix> <#DC2626>Failed to update.",
    val updateNoChange: String = "<prefix> <#F59E0B>Nothing changed.",
)
