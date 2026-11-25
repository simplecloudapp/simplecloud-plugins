package app.simplecloud.plugin.command.shared.command

import app.simplecloud.core.shared.adventure.tags
import app.simplecloud.core.shared.command.CloudSender
import app.simplecloud.core.shared.command.CommandExceptionHandler
import app.simplecloud.core.shared.command.HelpPage
import app.simplecloud.plugin.command.shared.CommandPlugin
import app.simplecloud.plugin.command.shared.command.commands.*
import app.simplecloud.plugin.command.shared.utilities.CommandPermissions
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
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
    )

    fun register() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .handler { context -> sendOverview(context.sender()) }
                .permission(Permission.permission(CommandPermissions.ROOT))
                .build(),
        )
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("help")
                .handler { context -> sendOverview(context.sender()) }
                .permission(Permission.permission(CommandPermissions.ROOT))
                .build(),
        )
        listOf("group", "server", "player", "ps").forEach { section ->
            manager.command(
                manager.commandBuilder("cloud", "sc", "simplecloud")
                    .literal(section)
                    .handler { context -> sendHelp(context.sender(), section) }
                    .permission(Permission.permission(CommandPermissions.ROOT))
                    .build(),
            )
        }
        GroupCommand(api, plugin, manager).register()
        ServerCommand(api, plugin, manager).register()
        PlayerCommand(api, plugin, manager).register()
        PersistentServerCommand(api, plugin, manager).register()
        InfoCommand(api, plugin, manager).register()
        CommandExceptionHandler(manager, plugin.configuration).register()
    }

    private fun sendOverview(sender: C) {
        val messages = plugin.config
        val help = messages.command.help
        val usages = usableEntries(sender)
        val sections = help.sections.filterKeys { section -> usages.any { it == section || it.startsWith("$section ") } }
        if (sections.isEmpty()) {
            sender.sendMessage(messages.msg(help.empty))
            return
        }
        sender.sendMessage(messages.msg(help.title))
        sections.forEach { (section, description) ->
            val sectionTags = tags("section" to section, "description" to description)
            sender.sendMessage(
                messages.msg(help.section, sectionTags)
                    .hoverEvent(HoverEvent.showText(messages.msg(help.sectionHover, sectionTags)))
                    .clickEvent(ClickEvent.runCommand("/cloud $section")),
            )
        }
    }

    private fun sendHelp(sender: C, section: String) {
        val messages = plugin.config
        val usages = usableEntries(sender).filter { it.startsWith("$section ") }
        if (usages.isEmpty()) {
            sender.sendMessage(messages.msg(messages.command.help.empty))
            return
        }
        sender.sendMessage(messages.msg(messages.command.help.sectionTitle, tags("section" to section)))
        HelpPage.entries(messages, messages.command.help, "/cloud", usages).forEach(sender::sendMessage)
    }

    private fun usableEntries(sender: C): List<String> =
        helpEntries.filter { (_, permission) -> sender.hasPermission(permission) }.map { it.first }
}
