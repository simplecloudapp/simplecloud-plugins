package app.simplecloud.plugin.notify.shared.config

import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.api.shared.config.AbstractMessageConfig
import app.simplecloud.plugin.api.shared.config.VersionedConfig
import app.simplecloud.plugin.notify.shared.utilities.config.ConfigVersion
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MessageConfig(
    override val version: Int = ConfigVersion.VERSION,
    val format: Format = Format(),
    override val variables: Map<String, String> = mapOf("prefix" to "<color:#0EA5E9><bold>⚡</bold></color>"),
    val command: Command = Command(),
    val notifications: Notifications = Notifications(),
) : VersionedConfig, AbstractMessageConfig()

@ConfigSerializable
data class Format(
    val date: String = "dd.MM.yyyy HH:mm:ss"
)

@ConfigSerializable
data class Command(
    val help: Help = Help(),
    val usage: Usage = Usage(),
    val permission: Permission = Permission(),
    val notify: Notify = Notify(),
    val set: SetNotify = SetNotify(),
    val list: NotifyList = NotifyList(),
    val reload: Reload = Reload(),
    val error: Error = Error()
)

@ConfigSerializable
data class Help(
    val title: String = "<prefix> <#0EA5E9>SimpleCloud Notify commands",
    val entry: String = "<#E2E8F0><command>",
    val empty: String = "<prefix> <#F59E0B>No notify commands are available for you."
)

@ConfigSerializable
data class Usage(
    val invalid: String = "<prefix> <#DC2626>Use <#F8FAFC><command> <#DC2626>instead.",
    val entry: String = "<#E2E8F0><command>",
    val invalidState: String = "<prefix> <#DC2626>Use <#F8FAFC>enable <#DC2626>or <#F8FAFC>disable<#DC2626>.",
    val invalidToggleValue: String = "<prefix> <#DC2626>Use <#F8FAFC>true <#DC2626>or <#F8FAFC>false<#DC2626>.",
    val missingPlayer: String = "<prefix> <#DC2626>Missing player <#F8FAFC><playername><#DC2626>."
)

@ConfigSerializable
data class Permission(
    val denied: String = "<prefix> <#DC2626>You do not have permission to use this command."
)

@ConfigSerializable
data class Notify(
    val enabled: String = "<prefix> <#A3E635>Server notifications are now <#F8FAFC>enabled<#A3E635>.",
    val disabled: String = "<prefix> <#A3E635>Server notifications are now <#F8FAFC>disabled<#A3E635>.",
    val alreadyEnabled: String = "<prefix> <#F59E0B>Server notifications are already <#F8FAFC>enabled<#F59E0B>.",
    val alreadyDisabled: String = "<prefix> <#F59E0B>Server notifications are already <#F8FAFC>disabled<#F59E0B>.",
    val type: NotifyType = NotifyType()
)

@ConfigSerializable
data class NotifyType(
    val enabled: String = "<prefix> <#F8FAFC><type> <#A3E635>notifications are now <#F8FAFC>enabled<#A3E635>.",
    val disabled: String = "<prefix> <#F8FAFC><type> <#A3E635>notifications are now <#F8FAFC>disabled<#A3E635>.",
    val alreadyEnabled: String = "<prefix> <#F8FAFC><type> <#F59E0B>notifications are already <#F8FAFC>enabled<#F59E0B>.",
    val alreadyDisabled: String = "<prefix> <#F8FAFC><type> <#F59E0B>notifications are already <#F8FAFC>disabled<#F59E0B>."
)

@ConfigSerializable
data class SetNotify(
    val enabled: String = "<prefix> <#A3E635>Server notifications are now <#F8FAFC>enabled <#A3E635>for <white><head:<playername>:true> <#F8FAFC><displayname><#A3E635>.",
    val disabled: String = "<prefix> <#A3E635>Server notifications are now <#F8FAFC>disabled <#A3E635>for <white><head:<playername>:true> <#F8FAFC><displayname><#A3E635>.",
    val alreadyEnabled: String = "<prefix> <#F59E0B>Server notifications are already <#F8FAFC>enabled <#F59E0B>for <white><head:<playername>:true> <#F8FAFC><displayname><#F59E0B>.",
    val alreadyDisabled: String = "<prefix> <#F59E0B>Server notifications are already <#F8FAFC>disabled <#F59E0B>for <white><head:<playername>:true> <#F8FAFC><displayname><#F59E0B>.",
    val type: SetNotifyType = SetNotifyType()
)

@ConfigSerializable
data class SetNotifyType(
    val enabled: String = "<prefix> <#F8FAFC><type> <#A3E635>notifications are now <#F8FAFC>enabled <#A3E635>for <white><head:<playername>:true> <#F8FAFC><displayname><#A3E635>.",
    val disabled: String = "<prefix> <#F8FAFC><type> <#A3E635>notifications are now <#F8FAFC>disabled <#A3E635>for <white><head:<playername>:true> <#F8FAFC><displayname><#A3E635>.",
    val alreadyEnabled: String = "<prefix> <#F8FAFC><type> <#F59E0B>notifications are already <#F8FAFC>enabled <#F59E0B>for <white><head:<playername>:true> <#F8FAFC><displayname><#F59E0B>.",
    val alreadyDisabled: String = "<prefix> <#F8FAFC><type> <#F59E0B>notifications are already <#F8FAFC>disabled <#F59E0B>for <white><head:<playername>:true> <#F8FAFC><displayname><#F59E0B>."
)

@ConfigSerializable
data class NotifyList(
    val title: String = "<prefix> <#0EA5E9>Your notifications <#475569>(click to toggle)",
    val entry: String = "<toggle><hover:show_text:'<#94A3B8>Click to toggle'><#475569>» <#E2E8F0><type> <#475569>- <state></hover></toggle>",
    val enabled: String = "<#A3E635>enabled",
    val disabled: String = "<#DC2626>disabled",
    val empty: String = "<prefix> <#F59E0B>There are no notifications available for you."
)

@ConfigSerializable
data class Reload(
    val config: ReloadConfig = ReloadConfig()
)

@ConfigSerializable
data class ReloadConfig(
    val success: String = "<prefix> <#A3E635>Notify configuration was reloaded.",
    val failed: String = "<prefix> <#DC2626>Notify configuration could not be reloaded."
)

@ConfigSerializable
data class Error(
    val playerNotFound: String = "<prefix> <#DC2626>Player <#F8FAFC><playername> <#DC2626>was not found.",
    val playerNotOnline: String = "<prefix> <#DC2626>Player <#F8FAFC><playername> <#DC2626>is not online.",
    val onlyPlayers: String = "<prefix> <#DC2626>This command can only be used by players.",
    val storageUnavailable: String = "<prefix> <#DC2626>Notify settings are currently unavailable.",
    val unknownType: String = "<prefix> <#DC2626>Unknown notification type <#F8FAFC><type><#DC2626>. Use <#F8FAFC>/scnotify list<#DC2626>.",
    val internal: String = "<prefix> <#DC2626>An internal error occurred. Try again later."
)

@ConfigSerializable
data class Notifications(
    val types: Map<String, String> = mapOf(
        "server.starting" to "Server starting",
        "server.available" to "Server available",
        "server.stopping" to "Server stopping",
        "server.stopped" to "Server stopped",
        "group.created" to "Group created",
        "group.deleted" to "Group deleted",
        "persistent-server.created" to "Persistent server created",
        "persistent-server.deleted" to "Persistent server deleted"
    ),
    val hover: Hover = Hover(),
    val server: ServerNotifications? = ServerNotifications(),
    val group: GroupNotifications? = GroupNotifications(),
    val persistentServer: PersistentServerNotifications? = PersistentServerNotifications()
)

@ConfigSerializable
data class Hover(
    val server: String = """
        <#E2E8F0>Server information
        <br><#94A3B8>Time<#475569>: <#E2E8F0><time>
        <br><#94A3B8>State<#475569>: <#E2E8F0><state>
        <br><#94A3B8>Address<#475569>: <#E2E8F0><ip>:<port>
        <br><#94A3B8>Players<#475569>: <#E2E8F0><players>/<max>
    """.trimIndent(),

    val group: String = """
        <#E2E8F0>Group information
        <br><#94A3B8>Time<#475569>: <#E2E8F0><time>
    """.trimIndent(),

    val persistentServer: String = """
        <#E2E8F0>Persistent server information
        <br><#94A3B8>Time<#475569>: <#E2E8F0><time>
    """.trimIndent()
)

@ConfigSerializable
data class ServerNotifications(
    val starting: String? = "<prefix> <#0EA5E9>Server <hover:show_text:'<notifications.hover.server>'><#F8FAFC><server></hover> <#0EA5E9>is now <#F8FAFC>starting<#0EA5E9>.",
    val available: String? = "<prefix> <#A3E635>Server <hover:show_text:'<notifications.hover.server>'><#F8FAFC><server></hover> <#A3E635>is now <#F8FAFC>available<#A3E635>.",
    val stopping: String? = "<prefix> <#F59E0B>Server <hover:show_text:'<notifications.hover.server>'><#F8FAFC><server></hover> <#F59E0B>is now <#F8FAFC>stopping<#F59E0B>.",
    val stopped: String? = "<prefix> <#DC2626>Server <hover:show_text:'<notifications.hover.server>'><#F8FAFC><server></hover> <#DC2626>is now <#F8FAFC>stopped<#DC2626>."
) {
    fun getByState(state: ServerState): String? {
        return when (state) {
            ServerState.STARTING -> starting
            ServerState.AVAILABLE -> available
            ServerState.STOPPING -> stopping
            else -> null
        }
    }
}

@ConfigSerializable
data class GroupNotifications(
    val created: String? = "<prefix> <#0EA5E9>Group <hover:show_text:'<notifications.hover.group>'><#F8FAFC><group></hover> <#0EA5E9>was created.",
    val deleted: String? = "<prefix> <#DC2626>Group <hover:show_text:'<notifications.hover.group>'><#F8FAFC><group></hover> <#DC2626>was deleted."
)

@ConfigSerializable
data class PersistentServerNotifications(
    val created: String? = "<prefix> <#0EA5E9>Persistent server <hover:show_text:'<notifications.hover.persistent-server>'><#F8FAFC><server></hover> <#0EA5E9>was created.",
    val deleted: String? = "<prefix> <#DC2626>Persistent server <hover:show_text:'<notifications.hover.persistent-server>'><#F8FAFC><server></hover> <#DC2626>was deleted."
)