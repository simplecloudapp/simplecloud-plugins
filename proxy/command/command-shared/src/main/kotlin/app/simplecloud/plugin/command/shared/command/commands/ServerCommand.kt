package app.simplecloud.plugin.command.shared.command.commands

import app.simplecloud.api.CloudApi
import app.simplecloud.api.group.Group
import app.simplecloud.api.server.Server
import app.simplecloud.plugin.command.shared.CommandPlugin
import app.simplecloud.plugin.command.shared.utilities.CommandPermissions
import app.simplecloud.plugin.command.shared.utilities.Formatter
import app.simplecloud.core.shared.adventure.tags
import app.simplecloud.core.shared.command.CloudSender
import app.simplecloud.core.shared.command.CloudSuggestions
import app.simplecloud.core.shared.controller.ApiUtils
import app.simplecloud.core.shared.controller.getDisplayName
import app.simplecloud.core.shared.controller.isRunning
import app.simplecloud.core.shared.controller.isStopped
import app.simplecloud.core.shared.pretty.PrettifyCase
import app.simplecloud.core.shared.pretty.StringPrettifier
import kotlinx.coroutines.future.*
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.parser.standard.IntegerParser.integerParser
import org.incendo.cloud.parser.standard.StringParser.StringMode
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.permission.Permission

class ServerCommand<C : CloudSender>(
    private val api: CloudApi,
    private val plugin: CommandPlugin,
    private val manager: CommandManager<C>,
) {

    fun register() {
        registerList()
        registerInfo()
        registerStart()
        registerStop()
        registerCommand()
    }

    private fun registerList() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("server")
                .literal("list")
                .optional("group", stringParser(), CloudSuggestions.groups(api))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val groupName = context.getOrDefault("group", null as String?)
                    val messages = plugin.config
                    val servers = if (groupName == null) {
                        api.server().allServers.await().filter { !it.isStopped() }
                    } else {
                        val group = findGroup(sender, groupName) ?: return@suspendingHandler
                        api.server().getServersByGroup(group.name).await().filter { !it.isStopped() }
                    }
                    if (servers.isEmpty()) {
                        sender.sendMessage(messages.msg(messages.command.server.list.empty))
                        return@suspendingHandler
                    }
                    sender.sendMessage(messages.msg(messages.command.server.list.title, tags("count" to servers.size)))
                    servers.forEach { server ->
                        val serverName = server.getDisplayName()
                        val value = "${server.playerCount ?: 0}/${server.maxPlayers}, ${server.state.name.lowercase()}"
                        sender.sendMessage(messages.msg(messages.command.server.list.entry, tags("server" to serverName, "value" to value)))
                    }
                }
                .permission(Permission.permission(CommandPermissions.SERVER_LIST))
                .build(),
        )
    }

    private fun registerInfo() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("server")
                .literal("info")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .required("id", integerParser(1), CloudSuggestions.serverIds(api))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val groupName = context.get<String>("group")
                    val id = context.get<Int>("id")
                    val group = findGroup(sender, groupName) ?: return@suspendingHandler
                    val server = findServer(sender, group, id) ?: return@suspendingHandler
                    val messages = plugin.config
                    val cpuUsage = server.cpuUsage
                    val memoryUsage = server.memoryUsage
                    val ip = server.ip
                    sender.sendMessage(messages.msg(messages.command.server.info.title, tags("server" to server.getDisplayName())))
                    listOf<Pair<String, Any?>>(
                        "State" to StringPrettifier.prettify(server.state.name, PrettifyCase.SENTENCE),
                        "Address" to if (ip == null) "-" else "$ip:${server.port ?: "-"}",
                        "Serverhost" to (server.serverhostId ?: "-"),
                        "Players" to "${server.playerCount ?: 0}/${server.maxPlayers}",
                        "Memory" to "${server.minMemory}-${server.maxMemory} MB",
                        "Memory usage" to if (memoryUsage == null) "-" else "%.0f MB".format(memoryUsage),
                        "CPU usage" to if (cpuUsage == null) "-" else "%.1f%%".format(cpuUsage),
                        "Uptime" to Formatter.since(server.createdAt),
                        "Last activity" to Formatter.date(server.lastActivity),
                        "Blueprint" to (server.blueprint?.name ?: "-"),
                    ).forEach { (key, value) ->
                        sender.sendMessage(messages.msg(messages.command.server.info.entry, tags("key" to key, "value" to value)))
                    }
                }
                .permission(Permission.permission(CommandPermissions.SERVER_INFO))
                .build(),
        )
    }

    private fun registerStart() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("server")
                .literal("start")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val groupName = context.get<String>("group")
                    val messages = plugin.config
                    val group = findGroup(sender, groupName) ?: return@suspendingHandler
                    api.group().requestServerStart(group).await()
                    sender.sendMessage(messages.msg(messages.command.server.start.requested, tags("group" to group.name)))
                }
                .permission(Permission.permission(CommandPermissions.SERVER_START))
                .build(),
        )
    }

    private fun registerStop() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("server")
                .literal("stop")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .required("id", integerParser(1), CloudSuggestions.serverIds(api))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val groupName = context.get<String>("group")
                    val id = context.get<Int>("id")
                    val messages = plugin.config
                    val group = findGroup(sender, groupName) ?: return@suspendingHandler
                    val server = findServer(sender, group, id) ?: return@suspendingHandler
                    if (!server.isRunning()) {
                        sender.sendMessage(messages.msg(messages.command.server.error.alreadyStopping, tags("group" to group.name, "id" to id)))
                        return@suspendingHandler
                    }
                    api.server().stopServer(server.serverId).await()
                    sender.sendMessage(messages.msg(messages.command.server.stop.success, tags("group" to group.name, "id" to id)))
                }
                .permission(Permission.permission(CommandPermissions.SERVER_STOP))
                .build(),
        )
    }

    private fun registerCommand() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("server")
                .literal("command")
                .required("server", stringParser(), CloudSuggestions.servers(api))
                .required("command", stringParser(StringMode.GREEDY))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val serverName = context.get<String>("server")
                    val command = context.get<String>("command").trim().removePrefix("/")
                    val messages = plugin.config
                    val server = ApiUtils.findServer(api, serverName)
                    if (server == null || !server.isRunning()) {
                        sender.sendMessage(messages.msg(messages.command.server.error.unknown, tags("server" to serverName)))
                        return@suspendingHandler
                    }
                    val tags = tags("server" to server.getDisplayName(), "command" to command)
                    if (command.isBlank()) {
                        sender.sendMessage(messages.msg(messages.command.server.error.commandEmpty, tags))
                        return@suspendingHandler
                    }
                    api.server().sendCommand(server.serverId, command).await()
                    sender.sendMessage(messages.msg(messages.command.server.command.success, tags))
                }
                .permission(Permission.permission(CommandPermissions.SERVER_COMMAND))
                .build(),
        )
    }

    private suspend fun findGroup(sender: C, groupName: String): Group? {
        val group = ApiUtils.findGroup(api, groupName)
        if (group == null) {
            val messages = plugin.config
            sender.sendMessage(messages.msg(messages.command.group.error.notFound, tags("group" to groupName)))
        }
        return group
    }

    private suspend fun findServer(sender: C, group: Group, id: Int): Server? {
        val server = ApiUtils.findServer(api, group, id)
        if (server == null) {
            val messages = plugin.config
            sender.sendMessage(messages.msg(messages.command.server.error.notFound, tags("group" to group.name, "id" to id)))
        }
        return server
    }
}
