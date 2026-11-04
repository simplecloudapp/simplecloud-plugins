package app.simplecloud.plugin.notify.shared.config

import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.core.shared.config.AbstractCommandMessageConfig
import app.simplecloud.plugin.core.shared.config.AbstractCommandMessages
import app.simplecloud.plugin.core.shared.config.CommandExceptionMessages
import app.simplecloud.plugin.core.shared.config.MessagePalette
import app.simplecloud.plugin.core.shared.config.VersionedConfig
import app.simplecloud.plugin.notify.shared.utilities.config.ConfigVersion
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MessageConfig(
    override val version: Int = ConfigVersion.VERSION,
    val format: Format = Format(),
    override val palette: Map<String, String> = MessagePalette.defaults,
    override val variables: Map<String, String> = mapOf("prefix" to MessagePalette.PREFIX),
    override val command: Command = Command(),
    val notifications: Notifications = Notifications(),
) : VersionedConfig, AbstractCommandMessageConfig()

@ConfigSerializable
data class Format(
    val date: String = "dd.MM.yyyy HH:mm:ss",
)

@ConfigSerializable
data class Command(
    val help: Help = Help(),
    val notify: Notify = Notify(),
    val set: SetNotify = SetNotify(),
    val list: NotifyList = NotifyList(),
    val reload: Reload = Reload(),
    val error: Error = Error(),
    override val exception: CommandExceptionMessages = CommandExceptionMessages(),
) : AbstractCommandMessages

@ConfigSerializable
data class Help(
    val title: String = "<prefix> <info>Notify commands",
    val entry: String = "<separator>•</separator> <interactive><command></interactive> <muted><arguments>",
    val hover: String = "<interactive>Click to type this command",
    val empty: String = "<prefix> <warning>You can't use any notify commands.",
)

@ConfigSerializable
data class Notify(
    val enabled: String = "<prefix> <success>Server notifications are now enabled.",
    val disabled: String = "<prefix> <success>Server notifications are now disabled.",
    val alreadyEnabled: String = "<prefix> <warning>Server notifications are already enabled.",
    val alreadyDisabled: String = "<prefix> <warning>Server notifications are already disabled.",
    val type: NotifyType = NotifyType(),
)

@ConfigSerializable
data class NotifyType(
    val enabled: String = "<prefix> <success><type> notifications are now enabled.",
    val disabled: String = "<prefix> <success><type> notifications are now disabled.",
    val alreadyEnabled: String = "<prefix> <warning><type> notifications are already enabled.",
    val alreadyDisabled: String = "<prefix> <warning><type> notifications are already disabled.",
)

@ConfigSerializable
data class SetNotify(
    val enabled: String = "<prefix> <success>Server notifications are now enabled for <white><head:<playername>:true></white> <displayname>.",
    val disabled: String = "<prefix> <success>Server notifications are now disabled for <white><head:<playername>:true></white> <displayname>.",
    val alreadyEnabled: String = "<prefix> <warning>Server notifications are already enabled for <white><head:<playername>:true></white> <displayname>.",
    val alreadyDisabled: String = "<prefix> <warning>Server notifications are already disabled for <white><head:<playername>:true></white> <displayname>.",
    val type: SetNotifyType = SetNotifyType(),
)

@ConfigSerializable
data class SetNotifyType(
    val enabled: String = "<prefix> <success><type> notifications are now enabled for <white><head:<playername>:true></white> <displayname>.",
    val disabled: String = "<prefix> <success><type> notifications are now disabled for <white><head:<playername>:true></white> <displayname>.",
    val alreadyEnabled: String = "<prefix> <warning><type> notifications are already enabled for <white><head:<playername>:true></white> <displayname>.",
    val alreadyDisabled: String = "<prefix> <warning><type> notifications are already disabled for <white><head:<playername>:true></white> <displayname>.",
)

@ConfigSerializable
data class NotifyList(
    val title: String = "<prefix> <info>Your notifications",
    val entry: String = "<toggle><hover:show_text:'<interactive>Click to toggle'><separator>•</separator> <interactive><type> <separator>(</separator><state><separator>)</separator></hover></toggle>",
    val enabled: String = "<success>enabled",
    val disabled: String = "<error>disabled",
    val empty: String = "<prefix> <warning>There are no notifications for you.",
)

@ConfigSerializable
data class Reload(
    val config: ReloadConfig = ReloadConfig(),
)

@ConfigSerializable
data class ReloadConfig(
    val success: String = "<prefix> <success>Reloaded the notify configuration!",
    val failed: String = "<prefix> <error>Couldn't reload the notify configuration. Check the console.",
)

@ConfigSerializable
data class Error(
    val playerNotFound: String = "<prefix> <error>Player <playername> was not found.",
    val playerNotOnline: String = "<prefix> <error>Player <playername> is not online.",
    val onlyPlayers: String = "<prefix> <error>Only players can use this command.",
    val storageUnavailable: String = "<prefix> <error>Notification settings are unavailable right now. Try again later.",
    val unknownType: String = "<prefix> <error><type> is not a notification type. Use /scnotify list.",
    val internal: String = "<prefix> <error>Something went wrong. Try again later.",
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
        "persistent-server.deleted" to "Persistent server deleted",
    ),
    val hover: Hover = Hover(),
    val server: ServerNotifications? = ServerNotifications(),
    val group: GroupNotifications? = GroupNotifications(),
    val persistentServer: PersistentServerNotifications? = PersistentServerNotifications(),
)

@ConfigSerializable
data class Hover(
    val server: String = """
        <brand><server>

        <muted>State: <highlight><state>
        <muted>Address: <highlight><ip>:<port>
        <muted>Players: <highlight><players>/<max>
        <muted>Time: <highlight><time>
    """.trimIndent(),

    val group: String = """
        <brand><group>

        <muted>Time: <highlight><time>
    """.trimIndent(),

    val persistentServer: String = """
        <brand><server>

        <muted>Time: <highlight><time>
    """.trimIndent(),
)

@ConfigSerializable
data class ServerNotifications(
    val starting: String? = "<prefix> <info>Server <hover:show_text:'<notifications.hover.server>'><server></hover> is starting.",
    val available: String? = "<prefix> <success>Server <hover:show_text:'<notifications.hover.server>'><server></hover> is now available.",
    val stopping: String? = "<prefix> <warning>Server <hover:show_text:'<notifications.hover.server>'><server></hover> is stopping.",
    val stopped: String? = "<prefix> <muted>Server <hover:show_text:'<notifications.hover.server>'><server></hover> has stopped.",
) {
    fun getByState(state: ServerState): String? = when (state) {
        ServerState.STARTING -> starting
        ServerState.AVAILABLE -> available
        ServerState.STOPPING -> stopping
        else -> null
    }
}

@ConfigSerializable
data class GroupNotifications(
    val created: String? = "<prefix> <info>Group <hover:show_text:'<notifications.hover.group>'><group></hover> was created.",
    val deleted: String? = "<prefix> <muted>Group <hover:show_text:'<notifications.hover.group>'><group></hover> was deleted.",
)

@ConfigSerializable
data class PersistentServerNotifications(
    val created: String? = "<prefix> <info>Persistent server <hover:show_text:'<notifications.hover.persistent-server>'><server></hover> was created.",
    val deleted: String? = "<prefix> <muted>Persistent server <hover:show_text:'<notifications.hover.persistent-server>'><server></hover> was deleted.",
)
