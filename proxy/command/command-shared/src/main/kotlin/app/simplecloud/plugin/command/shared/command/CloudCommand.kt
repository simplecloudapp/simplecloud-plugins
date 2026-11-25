package app.simplecloud.plugin.command.shared.command

import app.simplecloud.core.shared.command.CloudSender
import app.simplecloud.core.shared.command.CommandExceptionHandler
import app.simplecloud.core.shared.command.HelpPage
import app.simplecloud.plugin.command.shared.CommandPlugin
import app.simplecloud.plugin.command.shared.command.commands.*
import app.simplecloud.plugin.command.shared.utilities.CommandPermissions
import org.incendo.cloud.CommandManager
import org.incendo.cloud.permission.Permission

class CloudCommand<C : CloudSender>(
    private val manager: CommandManager<C>,
    private val plugin: CommandPlugin,
) {
    private val api = plugin.api

    private val helpEntries = listOf(
        "group list" to CommandPermissions.GROUP_LIST,
        "group info <group>" to CommandPermissions.GROUP_INFO,
        "group start <group> [count]" to CommandPermissions.GROUP_START,
        "group stop <group> [ids]" to CommandPermissions.GROUP_STOP,
        "group rollout <group>" to CommandPermissions.GROUP_ROLLOUT,
        "group rollout <group> status" to CommandPermissions.GROUP_ROLLOUT,
        "group rollout <group> cancel" to CommandPermissions.GROUP_ROLLOUT,
        "group queue <group>" to CommandPermissions.GROUP_QUEUE,
        "group queue <group> clear" to CommandPermissions.GROUP_QUEUE,
        "group enable <group>" to CommandPermissions.GROUP_ENABLE,
        "group disable <group>" to CommandPermissions.GROUP_DISABLE,
        "server list [group] [page]" to CommandPermissions.SERVER_LIST,
        "server info <server>" to CommandPermissions.SERVER_INFO,
        "server start <group>" to CommandPermissions.SERVER_START,
        "server stop <server>" to CommandPermissions.SERVER_STOP,
        "server command <server> <command>" to CommandPermissions.SERVER_COMMAND,
        "ps list" to CommandPermissions.PERSISTENT_SERVER_LIST,
        "ps info <name>" to CommandPermissions.PERSISTENT_SERVER_INFO,
        "ps enable <name>" to CommandPermissions.PERSISTENT_SERVER_ENABLE,
        "ps disable <name>" to CommandPermissions.PERSISTENT_SERVER_DISABLE,
        "player list [<group|ps|server> <target>] [page]" to CommandPermissions.PLAYER_LIST,
        "player info <player>" to CommandPermissions.PLAYER_INFO,
        "player send <player> <group|ps|server> <target> [mode]" to CommandPermissions.PLAYER_SEND,
        "player sendall <group|ps|server> <target> [mode]" to CommandPermissions.PLAYER_SEND,
        "player sendfrom <source> <group|ps|server> <target> [mode]" to CommandPermissions.PLAYER_SEND,
        "player message <player> <message>" to CommandPermissions.PLAYER_MESSAGE,
        "player kick <player> [reason]" to CommandPermissions.PLAYER_KICK,
        "info" to CommandPermissions.INFO,
        "reload" to CommandPermissions.RELOAD,
    )

    fun register() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .handler { context -> sendHelp(context.sender(), "") }
                .permission(Permission.permission(CommandPermissions.ROOT))
                .build(),
        )
        listOf("group", "server", "player", "ps").forEach { section ->
            manager.command(
                manager.commandBuilder("cloud", "sc", "simplecloud")
                    .literal(section)
                    .handler { context -> sendHelp(context.sender(), "$section ") }
                    .permission(Permission.permission(CommandPermissions.ROOT))
                    .build(),
            )
        }
        GroupCommand(api, plugin, manager).register()
        ServerCommand(api, plugin, manager).register()
        PlayerCommand(api, plugin, manager).register()
        PersistentServerCommand(api, plugin, manager).register()
        InfoCommand(api, plugin, manager).register()
        ReloadCommand(plugin, manager).register()
        CommandExceptionHandler(manager, plugin.configuration).register()
    }

    private fun sendHelp(sender: C, prefix: String) {
        val messages = plugin.config
        val usages = helpEntries
            .filter { (usage, permission) -> usage.startsWith(prefix) && sender.hasPermission(permission) }
            .map { it.first }
        if (usages.isEmpty()) {
            sender.sendMessage(messages.msg(messages.command.help.empty))
            return
        }
        sender.sendMessage(messages.msg(messages.command.help.title))
        HelpPage.entries(messages, messages.command.help, "/cloud", usages).forEach(sender::sendMessage)
    }
}
