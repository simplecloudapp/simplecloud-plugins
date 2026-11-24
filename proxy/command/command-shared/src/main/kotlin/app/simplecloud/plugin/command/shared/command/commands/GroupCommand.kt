package app.simplecloud.plugin.command.shared.command.commands

import app.simplecloud.api.CloudApi
import app.simplecloud.api.group.Group
import app.simplecloud.api.server.Server
import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.command.shared.CommandPlugin
import app.simplecloud.plugin.command.shared.utilities.CommandPermissions
import app.simplecloud.plugin.core.shared.adventure.tags
import app.simplecloud.plugin.core.shared.command.CloudSender
import app.simplecloud.plugin.core.shared.command.CloudSuggestions
import app.simplecloud.plugin.core.shared.controller.ApiUtils
import app.simplecloud.plugin.core.shared.pretty.PrettifyCase
import app.simplecloud.plugin.core.shared.pretty.StringPrettifier
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.parser.standard.IntegerParser.integerParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.permission.Permission
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

class GroupCommand<C : CloudSender>(
    private val api: CloudApi,
    private val plugin: CommandPlugin,
    private val manager: CommandManager<C>,
) {

    private val timeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault())
    private val stoppingStates = setOf(ServerState.STOPPING, ServerState.CLEANUP, ServerState.STOPPED)

    fun register() {
        registerList()
        registerInfo()
        registerStart()
        registerStop()
        registerRollout()
        registerRolloutStatus()
        registerRolloutCancel()
    }

    private fun registerList() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("group")
                .literal("list")
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val messages = plugin.messageConfiguration
                    val groups = api.group().allGroups.await()
                    if (groups.isEmpty()) {
                        sender.sendMessage(messages.msg(messages.command.group.list.empty))
                        return@suspendingHandler
                    }
                    val serversByGroup = api.server().allServers.await().filter { it.state != ServerState.STOPPED }.groupBy { it.serverGroupId }
                    sender.sendMessage(messages.msg(messages.command.group.list.title, tags("count" to groups.size)))
                    groups.forEach { group ->
                        val servers = serversByGroup[group.serverGroupId].orEmpty()
                        val online = servers.count { it.state == ServerState.AVAILABLE || it.state == ServerState.INGAME }
                        sender.sendMessage(
                            messages.msg(
                                messages.command.group.list.entry,
                                tags("group" to group.name, "online" to online, "total" to servers.size),
                            ),
                        )
                    }
                }
                .permission(Permission.permission(CommandPermissions.GROUP_LIST))
                .build(),
        )
    }

    private fun registerInfo() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("group")
                .literal("info")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val groupName = context.get<String>("group")
                    val messages = plugin.messageConfiguration
                    val group = ApiUtils.findGroup(api, groupName)
                    if (group == null) {
                        sender.sendMessage(messages.msg(messages.command.group.error.notFound, tags("group" to groupName)))
                        return@suspendingHandler
                    }
                    val servers = api.server().getServersByGroup(group.name).await().filter { it.state != ServerState.STOPPED }
                    sender.sendMessage(messages.msg(messages.command.group.info.title, tags("group" to group.name)))
                    sender.sendMessage(messages.msg(messages.command.group.info.entry, tags("key" to "Type", "value" to StringPrettifier.prettify(group.type.name, PrettifyCase.SENTENCE))))
                    sender.sendMessage(messages.msg(messages.command.group.info.entry, tags("key" to "Memory", "value" to "${group.minMemory}-${group.maxMemory} MB")))
                    sender.sendMessage(messages.msg(messages.command.group.info.entry, tags("key" to "Max players", "value" to group.maxPlayers)))
                    sender.sendMessage(messages.msg(messages.command.group.info.entry, tags("key" to "Running servers", "value" to servers.size)))
                }
                .permission(Permission.permission(CommandPermissions.GROUP_INFO))
                .build(),
        )
    }

    private fun registerStart() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("group")
                .literal("start")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .optional("count", integerParser(1, MAX_START_COUNT))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val groupName = context.get<String>("group")
                    val count = context.getOrDefault("count", 1)
                    val messages = plugin.messageConfiguration
                    val group = ApiUtils.findGroup(api, groupName)
                    if (group == null) {
                        sender.sendMessage(messages.msg(messages.command.group.error.notFound, tags("group" to groupName)))
                        return@suspendingHandler
                    }
                    val results = coroutineScope {
                        List(count) { async { runCatching { api.group().requestServerStart(group).await() } } }.awaitAll()
                    }
                    val failure = results.firstNotNullOfOrNull { it.exceptionOrNull() }
                    val started = results.count { it.isSuccess }
                    when {
                        failure == null -> sender.sendMessage(messages.msg(messages.command.group.start.success, tags("count" to count, "group" to group.name)))

                        started == 0 -> sender.sendMessage(messages.msg(messages.command.group.error.startFailed, tags("group" to group.name, "reason" to ApiUtils.reason(failure))))

                        else -> sender.sendMessage(
                            messages.msg(
                                messages.command.group.start.partial,
                                tags("count" to started, "total" to count, "group" to group.name, "reason" to ApiUtils.reason(failure)),
                            ),
                        )
                    }
                }
                .permission(Permission.permission(CommandPermissions.GROUP_START))
                .build(),
        )
    }

    private fun registerStop() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("group")
                .literal("stop")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .optional("id", integerParser(1), CloudSuggestions.serverIds(api))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val groupName = context.get<String>("group")
                    val id = context.getOrDefault("id", null as Int?)
                    val messages = plugin.messageConfiguration
                    val group = ApiUtils.findGroup(api, groupName)
                    if (group == null) {
                        sender.sendMessage(messages.msg(messages.command.group.error.notFound, tags("group" to groupName)))
                        return@suspendingHandler
                    }
                    val servers = api.server().getServersByGroup(group.name).await()
                    val matching = if (id == null) servers else servers.filter { it.numericalId == id }
                    if (id != null && matching.isEmpty()) {
                        sender.sendMessage(messages.msg(messages.command.server.error.notFound, tags("group" to group.name, "id" to id)))
                        return@suspendingHandler
                    }
                    val targets = matching.filter { it.state != ServerState.STOPPING && it.state != ServerState.CLEANUP && it.state != ServerState.STOPPED }
                    if (targets.isEmpty()) {
                        if (id == null) {
                            sender.sendMessage(messages.msg(messages.command.group.error.alreadyEmpty, tags("group" to group.name)))
                        } else {
                            sender.sendMessage(messages.msg(messages.command.server.error.alreadyStopping, tags("group" to group.name, "id" to id)))
                        }
                        return@suspendingHandler
                    }
                    val results = coroutineScope {
                        targets.map { server -> async { runCatching { api.server().stopServer(server.serverId).await() } } }.awaitAll()
                    }
                    val failure = results.firstNotNullOfOrNull { it.exceptionOrNull() }
                    val stopped = results.count { it.isSuccess }
                    when {
                        failure == null && id != null -> sender.sendMessage(messages.msg(messages.command.group.stop.successWithIds, tags("group" to group.name, "ids" to id)))
                        failure == null -> sender.sendMessage(messages.msg(messages.command.group.stop.success, tags("group" to group.name)))
                        stopped == 0 -> sender.sendMessage(messages.msg(messages.command.group.error.stopFailed, tags("group" to group.name, "reason" to ApiUtils.reason(failure))))
                        else -> sender.sendMessage(messages.msg(messages.command.group.stop.partial, tags("count" to stopped, "total" to targets.size, "group" to group.name, "reason" to ApiUtils.reason(failure))))
                    }
                }
                .permission(Permission.permission(CommandPermissions.GROUP_STOP))
                .build(),
        )
    }

    private fun registerRollout() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("group")
                .literal("rollout")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .suspendingHandler(plugin.scope) { context ->
                    startRollout(context.sender(), context.get("group"))
                }
                .permission(Permission.permission(CommandPermissions.GROUP_ROLLOUT))
                .build(),
        )
    }

    private fun registerRolloutStatus() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("group")
                .literal("rollout")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .literal("status")
                .suspendingHandler(plugin.scope) { context ->
                    showRolloutStatus(context.sender(), context.get("group"))
                }
                .permission(Permission.permission(CommandPermissions.GROUP_ROLLOUT))
                .build(),
        )
    }

    private fun registerRolloutCancel() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("group")
                .literal("rollout")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .literal("cancel")
                .suspendingHandler(plugin.scope) { context ->
                    cancelRollout(context.sender(), context.get("group"))
                }
                .permission(Permission.permission(CommandPermissions.GROUP_ROLLOUT))
                .build(),
        )
    }

    private suspend fun startRollout(sender: C, groupName: String) {
        val messages = plugin.messageConfiguration
        val group = findGroup(sender, groupName) ?: return
        if (api.group().getRollingRestartStatus(group.serverGroupId).await().active()) {
            sender.sendMessage(messages.msg(messages.command.group.rollout.error.alreadyActive, tags("group" to group.name)))
            return
        }
        if (runningServers(group).isEmpty()) {
            sender.sendMessage(messages.msg(messages.command.group.rollout.error.noServers, tags("group" to group.name)))
            return
        }
        runCatching { api.group().startRollingRestart(group.serverGroupId).await() }
            .onSuccess { sender.sendMessage(messages.msg(messages.command.group.rollout.started, tags("group" to group.name))) }
            .onFailure {
                sender.sendMessage(
                    messages.msg(
                        messages.command.group.rollout.error.startFailed,
                        tags("group" to group.name, "reason" to ApiUtils.reason(it)),
                    ),
                )
            }
    }

    private suspend fun showRolloutStatus(sender: C, groupName: String) {
        val messages = plugin.messageConfiguration
        val group = findGroup(sender, groupName) ?: return
        val status = runCatching { api.group().getRollingRestartStatus(group.serverGroupId).await() }.getOrElse {
            sender.sendMessage(
                messages.msg(
                    messages.command.group.rollout.error.statusFailed,
                    tags("group" to group.name, "reason" to ApiUtils.reason(it)),
                ),
            )
            return
        }
        if (!status.active()) {
            sender.sendMessage(messages.msg(messages.command.group.rollout.status.inactive, tags("group" to group.name)))
            return
        }
        val startedAt = parseInstant(status.timestamp())
        val servers = runningServers(group)
        val renewed = if (startedAt == null) 0 else servers.count { isRenewed(it, startedAt) }
        val runningFor = if (startedAt == null) "-" else Duration.between(startedAt, Instant.now()).toSeconds().seconds
        val startedAtText = if (startedAt == null) "-" else timeFormatter.format(startedAt)
        sender.sendMessage(messages.msg(messages.command.group.rollout.status.title, tags("group" to group.name)))
        listOf<Pair<String, Any?>>(
            "Progress" to "$renewed/${servers.size} servers",
            "Running for" to runningFor,
            "Started at" to startedAtText,
        ).forEach { (key, value) ->
            sender.sendMessage(messages.msg(messages.command.group.rollout.status.entry, tags("key" to key, "value" to value)))
        }
    }

    private suspend fun cancelRollout(sender: C, groupName: String) {
        val messages = plugin.messageConfiguration
        val group = findGroup(sender, groupName) ?: return
        if (!api.group().getRollingRestartStatus(group.serverGroupId).await().active()) {
            sender.sendMessage(messages.msg(messages.command.group.rollout.error.notActive, tags("group" to group.name)))
            return
        }
        runCatching { api.group().cancelRollingRestart(group.serverGroupId).await() }
            .onSuccess { sender.sendMessage(messages.msg(messages.command.group.rollout.cancelled, tags("group" to group.name))) }
            .onFailure {
                sender.sendMessage(
                    messages.msg(
                        messages.command.group.rollout.error.cancelFailed,
                        tags("group" to group.name, "reason" to ApiUtils.reason(it)),
                    ),
                )
            }
    }

    private suspend fun findGroup(sender: C, groupName: String): Group? {
        val group = ApiUtils.findGroup(api, groupName)
        if (group == null) {
            val messages = plugin.messageConfiguration
            sender.sendMessage(messages.msg(messages.command.group.error.notFound, tags("group" to groupName)))
        }
        return group
    }

    private suspend fun runningServers(group: Group): List<Server> =
        api.server().getServersByGroup(group.name).await().filter { it.state !in stoppingStates }

    private fun isRenewed(server: Server, startedAt: Instant): Boolean {
        val createdAt = parseInstant(server.createdAt) ?: return false
        return !createdAt.isBefore(startedAt)
    }

    private fun parseInstant(value: String?): Instant? {
        if (value.isNullOrBlank()) return null
        return runCatching { Instant.parse(value) }.getOrNull()
    }

    companion object {
        private const val MAX_START_COUNT = 50
    }
}
