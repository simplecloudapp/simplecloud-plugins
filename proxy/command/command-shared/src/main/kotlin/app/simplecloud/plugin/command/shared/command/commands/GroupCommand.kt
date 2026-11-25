package app.simplecloud.plugin.command.shared.command.commands

import app.simplecloud.api.CloudApi
import app.simplecloud.api.group.Group
import app.simplecloud.api.group.GroupStartQueueItem
import app.simplecloud.api.group.GroupStartQueueItemStatus
import app.simplecloud.api.group.RollingRestartStatus
import app.simplecloud.api.group.UpdateGroupRequest
import app.simplecloud.api.server.Server
import app.simplecloud.core.shared.adventure.tags
import app.simplecloud.core.shared.command.CloudSender
import app.simplecloud.core.shared.command.CloudSuggestions
import app.simplecloud.core.shared.controller.ApiUtils
import app.simplecloud.core.shared.controller.isJoinable
import app.simplecloud.core.shared.controller.getMaxServers
import app.simplecloud.core.shared.controller.getMinServers
import app.simplecloud.core.shared.controller.isRunning
import app.simplecloud.core.shared.controller.isStopped
import app.simplecloud.core.shared.pretty.PrettifyCase
import app.simplecloud.core.shared.pretty.StringPrettifier
import app.simplecloud.plugin.command.shared.CommandPlugin
import app.simplecloud.plugin.command.shared.utilities.CommandPermissions
import kotlinx.coroutines.*
import kotlinx.coroutines.future.*
import net.kyori.adventure.text.Component
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.parser.standard.IntegerParser.integerParser
import org.incendo.cloud.parser.standard.StringParser.StringMode
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.permission.Permission
import java.time.Instant

class GroupCommand<C : CloudSender>(
    private val api: CloudApi,
    private val plugin: CommandPlugin,
    private val manager: CommandManager<C>,
) {

    private val formatter get() = plugin.formatter

    fun register() {
        registerList()
        registerInfo()
        registerStart()
        registerStop()
        registerRollout()
        registerRolloutStatus()
        registerRolloutCancel()
        registerQueue()
        registerQueueClear()
        registerActivation("enable", true, CommandPermissions.GROUP_ENABLE)
        registerActivation("disable", false, CommandPermissions.GROUP_DISABLE)
    }

    private fun registerList() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("group")
                .literal("list")
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val messages = plugin.config
                    val groups = api.group().allGroups.await()
                    if (groups.isEmpty()) {
                        sender.sendMessage(messages.msg(messages.command.group.list.empty))
                        return@suspendingHandler
                    }
                    val serversByGroup = api.server().allServers.await().filter { !it.isStopped() }.groupBy { it.serverGroupId }
                    sender.sendMessage(messages.msg(messages.command.group.list.title, tags("count" to groups.size)))
                    groups.sortedBy { it.name.lowercase() }.forEach { group ->
                        val servers = serversByGroup[group.serverGroupId].orEmpty()
                        val online = servers.count { it.isJoinable() }
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
                    val messages = plugin.config
                    val group = findGroup(sender, groupName) ?: return@suspendingHandler
                    val servers = ApiUtils.getRunningServers(api, group)
                    val rollout = runCatching { api.group().getRollingRestartStatus(group.serverGroupId).await() }.getOrNull()
                    sender.sendMessage(messages.msg(messages.command.group.info.title, tags("group" to group.name)))
                    listOf<Pair<String, Any?>>(
                        "Type" to StringPrettifier.prettify(group.type.name, PrettifyCase.SENTENCE),
                        "Active" to formatter.yesNo(group.isActive()),
                        "Priority" to (group.priority ?: formatter.empty),
                        "Tags" to group.tags.orEmpty().joinToString(", ").ifEmpty { formatter.empty },
                        "Memory" to formatter.memory(group.minMemory, group.maxMemory),
                        "Max players" to (group.maxPlayers ?: formatter.empty),
                        "Running servers" to servers.size,
                        "Scaling" to scalingText(group),
                        "Rollout" to rolloutText(rollout),
                    ).forEach { (key, value) ->
                        sender.sendMessage(messages.msg(messages.command.group.info.entry, tags("key" to key, "value" to value)))
                    }
                }
                .permission(Permission.permission(CommandPermissions.GROUP_INFO))
                .build(),
        )
    }

    private fun scalingText(group: Group): String {
        val scaling = group.scaling ?: return formatter.empty
        val servers = "${group.getMinServers()}-${group.getMaxServers() ?: formatter.empty} servers"
        val mode = scaling.scalingMode() ?: return servers
        return "$servers, ${StringPrettifier.prettify(mode.name, PrettifyCase.SENTENCE)}"
    }

    private fun rolloutText(status: RollingRestartStatus?): String = when {
        status == null -> formatter.empty
        status.active() -> "Running since ${formatter.date(status.timestamp())}"
        else -> "Inactive"
    }

    private fun registerStart() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("group")
                .literal("start")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .optional("count", integerParser(1, MAX_START_COUNT))
                .suspendingHandler(plugin.scope) { context ->
                    startServers(context.sender(), context.get("group"), context.getOrDefault("count", 1))
                }
                .permission(Permission.permission(CommandPermissions.GROUP_START))
                .build(),
        )
    }

    private suspend fun startServers(sender: C, groupName: String, count: Int) {
        val messages = plugin.config
        val group = findGroup(sender, groupName) ?: return
        if (group.isActive() == false) {
            sender.sendMessage(messages.msg(messages.command.group.error.disabled, tags("group" to group.name)))
            return
        }
        val maxServers = group.getMaxServers()
        if (maxServers != null) {
            val running = ApiUtils.getRunningServers(api, group).size + ApiUtils.getQueuedStarts(api, group)
            val available = (maxServers - running).coerceAtLeast(0)
            if (count > available) {
                sender.sendMessage(
                    messages.msg(
                        messages.command.group.error.limitReached,
                        tags("group" to group.name, "available" to available, "running" to running, "max" to maxServers),
                    ),
                )
                return
            }
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

    private fun registerStop() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("group")
                .literal("stop")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .optional("ids", stringParser(StringMode.GREEDY), CloudSuggestions.serverIds(api))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val group = findGroup(sender, context.get("group")) ?: return@suspendingHandler
                    val ids = context.getOrDefault("ids", null as String?)
                    if (ids == null) {
                        stopAllServers(sender, group)
                    } else {
                        stopServers(sender, group, ids)
                    }
                }
                .permission(Permission.permission(CommandPermissions.GROUP_STOP))
                .build(),
        )
    }

    private suspend fun stopAllServers(sender: C, group: Group) {
        val messages = plugin.config
        val count = api.group().stopAllServers(group.serverGroupId).await()
        if (count == 0) {
            sender.sendMessage(messages.msg(messages.command.group.error.alreadyEmpty, tags("group" to group.name)))
            return
        }
        sender.sendMessage(messages.msg(messages.command.group.stop.success, tags("count" to count, "group" to group.name)))
    }

    private suspend fun stopServers(sender: C, group: Group, input: String) {
        val messages = plugin.config
        val ids = input.split(' ', ',').filter { it.isNotBlank() }.map { it.toIntOrNull() }
        if (ids.isEmpty() || null in ids) {
            sender.sendMessage(messages.msg(messages.command.group.error.invalidIds, tags("ids" to input.trim())))
            return
        }
        val requested = ids.filterNotNull().toSortedSet()
        val servers = api.server().getServersByGroup(group.name).await().filter { it.numericalId in requested }
        val missing = requested - servers.map { it.numericalId }.toSet()
        if (missing.isNotEmpty()) {
            sender.sendMessage(messages.msg(messages.command.group.error.serversNotFound, tags("group" to group.name, "ids" to missing.joinToString(", "))))
            return
        }
        val targets = servers.filter { it.isRunning() }
        if (targets.isEmpty()) {
            sender.sendMessage(messages.msg(messages.command.group.error.alreadyStopping, tags("group" to group.name, "ids" to requested.joinToString(", "))))
            return
        }
        val results = coroutineScope {
            targets.map { server -> async { runCatching { api.server().stopServer(server.serverId).await() } } }.awaitAll()
        }
        val failure = results.firstNotNullOfOrNull { it.exceptionOrNull() }
        val stopped = results.count { it.isSuccess }
        val stoppedIds = targets.map { it.numericalId }.sorted().joinToString(", ")
        when {
            failure == null -> sender.sendMessage(messages.msg(messages.command.group.stop.successWithIds, tags("group" to group.name, "ids" to stoppedIds)))
            stopped == 0 -> sender.sendMessage(messages.msg(messages.command.group.error.stopFailed, tags("group" to group.name, "reason" to ApiUtils.reason(failure))))
            else -> sender.sendMessage(messages.msg(messages.command.group.stop.partial, tags("count" to stopped, "total" to targets.size, "group" to group.name, "reason" to ApiUtils.reason(failure))))
        }
    }

    private fun registerRollout() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("group")
                .literal("rollout")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val groupName = context.get<String>("group")
                    val messages = plugin.config
                    val group = findGroup(sender, groupName) ?: return@suspendingHandler
                    if (api.group().getRollingRestartStatus(group.serverGroupId).await().active()) {
                        sender.sendMessage(messages.msg(messages.command.group.rollout.error.alreadyActive, tags("group" to group.name)))
                        return@suspendingHandler
                    }
                    if (ApiUtils.getRunningServers(api, group).isEmpty()) {
                        sender.sendMessage(messages.msg(messages.command.group.rollout.error.noServers, tags("group" to group.name)))
                        return@suspendingHandler
                    }
                    api.group().startRollingRestart(group.serverGroupId).await()
                    sender.sendMessage(messages.msg(messages.command.group.rollout.started, tags("group" to group.name)))
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
                    val sender = context.sender()
                    val groupName = context.get<String>("group")
                    val messages = plugin.config
                    val group = findGroup(sender, groupName) ?: return@suspendingHandler
                    val status = api.group().getRollingRestartStatus(group.serverGroupId).await()
                    if (!status.active()) {
                        sender.sendMessage(messages.msg(messages.command.group.rollout.status.inactive, tags("group" to group.name)))
                        return@suspendingHandler
                    }
                    val startedAt = formatter.parseTime(status.timestamp())
                    val servers = ApiUtils.getRunningServers(api, group)
                    val renewed = if (startedAt == null) 0 else servers.count { isRenewed(it, startedAt) }
                    sender.sendMessage(messages.msg(messages.command.group.rollout.status.title, tags("group" to group.name)))
                    listOf<Pair<String, Any?>>(
                        "Progress" to "$renewed/${servers.size} servers",
                        "Running for" to formatter.since(status.timestamp()),
                        "Started at" to formatter.date(status.timestamp()),
                    ).forEach { (key, value) ->
                        sender.sendMessage(messages.msg(messages.command.group.rollout.status.entry, tags("key" to key, "value" to value)))
                    }
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
                    val sender = context.sender()
                    val groupName = context.get<String>("group")
                    val messages = plugin.config
                    val group = findGroup(sender, groupName) ?: return@suspendingHandler
                    if (!api.group().getRollingRestartStatus(group.serverGroupId).await().active()) {
                        sender.sendMessage(messages.msg(messages.command.group.rollout.error.notActive, tags("group" to group.name)))
                        return@suspendingHandler
                    }
                    api.group().cancelRollingRestart(group.serverGroupId).await()
                    sender.sendMessage(messages.msg(messages.command.group.rollout.cancelled, tags("group" to group.name)))
                }
                .permission(Permission.permission(CommandPermissions.GROUP_ROLLOUT))
                .build(),
        )
    }

    private fun registerQueue() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("group")
                .literal("queue")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .suspendingHandler(plugin.scope) { context ->
                    showQueue(context.sender(), context.get("group"))
                }
                .permission(Permission.permission(CommandPermissions.GROUP_QUEUE))
                .build(),
        )
    }

    private fun registerQueueClear() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("group")
                .literal("queue")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .literal("clear")
                .suspendingHandler(plugin.scope) { context ->
                    clearQueue(context.sender(), context.get("group"))
                }
                .permission(Permission.permission(CommandPermissions.GROUP_QUEUE))
                .build(),
        )
    }

    private suspend fun showQueue(sender: C, groupName: String) {
        val messages = plugin.config
        val group = findGroup(sender, groupName) ?: return
        val queue = ApiUtils.findStartQueue(api, group)
        if (queue == null || queue.starts().isEmpty()) {
            sender.sendMessage(messages.msg(messages.command.group.queue.empty, tags("group" to group.name)))
            return
        }
        val starts = queue.starts()
        sender.sendMessage(messages.msg(messages.command.group.queue.title, tags("group" to group.name, "count" to starts.size)))
        listOf(
            "Queued" to queue.queuedStarts(),
            "Failed" to queue.failedStarts(),
            "Total" to queue.totalStarts(),
        ).forEach { (key, value) ->
            sender.sendMessage(messages.msg(messages.command.group.queue.entry, tags("key" to key, "value" to value)))
        }
        starts.take(MAX_QUEUE_ENTRIES).forEach { start -> sender.sendMessage(queueEntry(start)) }
        if (starts.size > MAX_QUEUE_ENTRIES) {
            sender.sendMessage(messages.msg(messages.command.group.queue.more, tags("count" to starts.size - MAX_QUEUE_ENTRIES)))
        }
    }

    private fun queueEntry(start: GroupStartQueueItem): Component {
        val messages = plugin.config
        val date = formatter.date(start.createdAt())
        if (start.status() == GroupStartQueueItemStatus.FAILED) {
            val reason = start.failureReason() ?: "unknown reason"
            return messages.msg(messages.command.group.queue.failed, tags("date" to date, "reason" to reason))
        }
        return messages.msg(messages.command.group.queue.pending, tags("date" to date))
    }

    private suspend fun clearQueue(sender: C, groupName: String) {
        val messages = plugin.config
        val group = findGroup(sender, groupName) ?: return
        val queued = ApiUtils.getQueuedStarts(api, group)
        if (queued == 0) {
            sender.sendMessage(messages.msg(messages.command.group.queue.alreadyEmpty, tags("group" to group.name)))
            return
        }
        api.group().clearServerStartQueue(group).await()
        sender.sendMessage(messages.msg(messages.command.group.queue.cleared, tags("count" to queued, "group" to group.name)))
    }

    private fun registerActivation(literal: String, active: Boolean, permission: String) {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("group")
                .literal(literal)
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .suspendingHandler(plugin.scope) { context ->
                    setActive(context.sender(), context.get("group"), active)
                }
                .permission(Permission.permission(permission))
                .build(),
        )
    }

    private suspend fun setActive(sender: C, groupName: String, active: Boolean) {
        val messages = plugin.config
        val activation = messages.command.group.activation
        val group = findGroup(sender, groupName) ?: return
        if (group.isActive() == active) {
            val message = if (active) activation.alreadyEnabled else activation.alreadyDisabled
            sender.sendMessage(messages.msg(message, tags("group" to group.name)))
            return
        }
        api.group().updateGroup(group.serverGroupId, UpdateGroupRequest.builder().active(active).build()).await()
        val message = if (active) activation.enabled else activation.disabled
        sender.sendMessage(messages.msg(message, tags("group" to group.name)))
    }

    private suspend fun findGroup(sender: C, groupName: String): Group? {
        val group = ApiUtils.findGroup(api, groupName)
        if (group == null) {
            val messages = plugin.config
            sender.sendMessage(messages.msg(messages.command.group.error.notFound, tags("group" to groupName)))
        }
        return group
    }

    private fun isRenewed(server: Server, startedAt: Instant): Boolean {
        val createdAt = formatter.parseTime(server.createdAt) ?: return false
        return !createdAt.isBefore(startedAt)
    }

    companion object {
        private const val MAX_START_COUNT = 50
        private const val MAX_QUEUE_ENTRIES = 10
    }
}
