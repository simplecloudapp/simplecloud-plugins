package app.simplecloud.plugin.command.shared.command

import app.simplecloud.plugin.command.shared.CommandPlugin
import app.simplecloud.plugin.command.shared.command.commands.GroupCommand
import app.simplecloud.plugin.command.shared.command.commands.PlayerCommand
import app.simplecloud.plugin.command.shared.command.commands.ReloadCommand
import app.simplecloud.plugin.command.shared.command.commands.ServerCommand
import app.simplecloud.plugin.command.shared.utilities.CommandPermissions
import app.simplecloud.plugin.core.shared.command.CloudSender
import app.simplecloud.plugin.core.shared.command.CommandExceptionHandler
import app.simplecloud.plugin.core.shared.command.HelpPage
import org.incendo.cloud.CommandManager
import org.incendo.cloud.permission.Permission

class CloudCommandHandler<C : CloudSender>(
    private val manager: CommandManager<C>,
    private val plugin: CommandPlugin,
) {
    private val api = plugin.api

    fun register() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .handler { context ->
                    val sender = context.sender()
                    val messages = plugin.messageConfiguration
                    val entries = listOf(
                        "group list" to CommandPermissions.GROUP_LIST,
                        "group info <group>" to CommandPermissions.GROUP_INFO,
                        "group start <group> [count]" to CommandPermissions.GROUP_START,
                        "group stop <group> [id]" to CommandPermissions.GROUP_STOP,
                        "server list [group]" to CommandPermissions.SERVER_LIST,
                        "server info <group> <id>" to CommandPermissions.SERVER_INFO,
                        "server start <group>" to CommandPermissions.SERVER_START,
                        "server stop <group> <id>" to CommandPermissions.SERVER_STOP,
                        "player list [<group|ps> <target>]" to CommandPermissions.PLAYER_LIST,
                        "player info <player>" to CommandPermissions.PLAYER_INFO,
                        "player send <player> <group|ps> <target>" to CommandPermissions.PLAYER_SEND,
                        "player sendall <group|ps> <target>" to CommandPermissions.PLAYER_SEND,
                        "player sendfrom <source> <group|ps> <target>" to CommandPermissions.PLAYER_SEND,
                        "player message <player> <message>" to CommandPermissions.PLAYER_MESSAGE,
                        "reload" to CommandPermissions.RELOAD,
                    ).filter { (_, permission) -> context.sender().hasPermission(permission) }

                    if (entries.isEmpty()) {
                        sender.sendMessage(messages.msg(messages.command.help.empty))
                        return@handler
                    }

                    sender.sendMessage(messages.msg(messages.command.help.title))
                    HelpPage.entries(messages, messages.command.help, "/cloud", entries.map { it.first })
                        .forEach(sender::sendMessage)
                }
                .permission(Permission.permission(CommandPermissions.ROOT))
                .build(),
        )
        GroupCommand(api, plugin, manager).register()
        ServerCommand(api, plugin, manager).register()
        PlayerCommand(api, plugin, manager).register()
        ReloadCommand(plugin, manager).register()
        CommandExceptionHandler(manager, plugin.config).register()
    }
}
