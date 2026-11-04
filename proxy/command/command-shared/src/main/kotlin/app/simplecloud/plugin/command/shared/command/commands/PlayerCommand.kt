package app.simplecloud.plugin.command.shared.command.commands

import app.simplecloud.api.CloudApi
import app.simplecloud.api.base.ServerBase
import app.simplecloud.api.group.Group
import app.simplecloud.api.persistentserver.PersistentServer
import app.simplecloud.api.player.CloudPlayer
import app.simplecloud.api.player.CloudPlayer.ConnectResult
import app.simplecloud.api.server.Server
import app.simplecloud.plugin.command.shared.CommandPlugin
import app.simplecloud.plugin.command.shared.utilities.CommandPermissions
import app.simplecloud.plugin.core.shared.adventure.tags
import app.simplecloud.plugin.core.shared.command.CloudSender
import app.simplecloud.plugin.core.shared.command.CloudSuggestions
import app.simplecloud.plugin.core.shared.command.CloudSuggestions.TARGET_TYPE_GROUP
import app.simplecloud.plugin.core.shared.command.CloudSuggestions.TARGET_TYPE_PERSISTENT_SERVER
import app.simplecloud.plugin.core.shared.controller.ApiUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.future.*
import net.kyori.adventure.text.Component
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.parser.standard.StringParser.StringMode
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.permission.Permission
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration.Companion.seconds

class PlayerCommand<C : CloudSender>(
    private val api: CloudApi,
    private val plugin: CommandPlugin,
    private val manager: CommandManager<C>,
) {

    fun register() {
        registerList()
        registerInfo()
        registerSend()
        registerSendAll()
        registerSendFrom()
        registerMessage()
    }

    private fun registerList() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("player")
                .literal("list")
                .optional("targetType", stringParser(), CloudSuggestions.targetTypes())
                .optional("target", stringParser(), CloudSuggestions.targets(api))
                .suspendingHandler(plugin.scope) { context ->
                    val targetType = context.optional<String>("targetType").orElse(null)
                    val target = context.optional<String>("target").orElse(null)
                    listPlayers(context.sender(), targetType, target)
                }
                .permission(Permission.permission(CommandPermissions.PLAYER_LIST))
                .build(),
        )
    }

    private suspend fun listPlayers(sender: C, targetType: String?, target: String?) {
        val messages = plugin.messageConfiguration
        if (targetType != null && !isTargetTypeValid(targetType)) {
            sender.sendMessage(messages.msg(messages.command.usage.invalidTargetType))
            return
        }
        if (targetType != null && target == null) {
            sender.sendMessage(
                messages.msg(messages.command.exception.syntax, tags("command" to "/cloud player list [<group|ps> <target>]")),
            )
            return
        }
        val resolvedTarget = if (targetType == null || target == null) null else resolveTarget(targetType, target)
        if (target != null && resolvedTarget == null) {
            sender.sendMessage(messages.msg(messages.command.player.error.targetNotFound, tags("target" to target)))
            return
        }
        val players = if (resolvedTarget == null) api.player().onlinePlayers.await() else playersForTarget(resolvedTarget)
        if (players.isEmpty()) {
            sender.sendMessage(messages.msg(messages.command.player.list.empty))
            return
        }
        sender.sendMessage(messages.msg(messages.command.player.list.title, tags("count" to players.size)))
        players.sortedBy { it.name.lowercase() }.forEach { player ->
            sender.sendMessage(
                messages.msg(
                    messages.command.player.list.entry,
                    tags(
                        "playername" to player.name,
                        "displayname" to player.displayName,
                        "value" to player.connectedServerName,
                    ),
                ),
            )
        }
    }

    private fun registerInfo() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("player")
                .literal("info")
                .required("player", stringParser(), CloudSuggestions.onlinePlayers(api))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val playerName = context.get<String>("player")
                    val messages = plugin.messageConfiguration
                    val player = api.player().find(playerName).await().orElse(null)
                    if (player == null) {
                        sender.sendMessage(
                            messages.msg(messages.command.player.error.notFound, tags("playername" to playerName)),
                        )
                        return@suspendingHandler
                    }
                    sender.sendMessage(
                        messages.msg(
                            messages.command.player.info.title,
                            tags(
                                "playername" to player.name,
                                "displayname" to player.displayName,
                            ),
                        ),
                    )
                    listOf<Pair<String, Any?>>(
                        "Name" to player.name,
                        "Display name" to player.displayName,
                        "Online" to if (player.isOnline) "Yes" else "No",
                        "Server" to player.connectedServerName,
                        "Proxy" to player.connectedProxyName,
                        "Online time" to player.onlineTimeSeconds.seconds,
                        "First seen" to player.firstSeen,
                        "Last seen" to player.lastSeen,
                    ).forEach { (key, value) ->
                        sender.sendMessage(
                            messages.msg(messages.command.player.info.entry, tags("key" to key, "value" to value)),
                        )
                    }
                }
                .permission(Permission.permission(CommandPermissions.PLAYER_INFO))
                .build(),
        )
    }

    private fun registerSend() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("player")
                .literal("send")
                .required("player", stringParser(), CloudSuggestions.onlinePlayers(api))
                .required("targetType", stringParser(), CloudSuggestions.targetTypes())
                .required("target", stringParser(), CloudSuggestions.targets(api))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val playerName = context.get<String>("player")
                    val targetType = context.get<String>("targetType")
                    val target = context.get<String>("target")
                    val messages = plugin.messageConfiguration
                    if (!isTargetTypeValid(targetType)) {
                        sender.sendMessage(messages.msg(messages.command.usage.invalidTargetType))
                        return@suspendingHandler
                    }
                    val player = api.player().find(playerName).await().orElse(null)
                    if (player == null) {
                        sender.sendMessage(
                            messages.msg(messages.command.player.error.notFound, tags("playername" to playerName)),
                        )
                        return@suspendingHandler
                    }
                    if (!player.isOnline) {
                        sender.sendMessage(
                            messages.msg(messages.command.player.error.notOnline, tags("playername" to player.name)),
                        )
                        return@suspendingHandler
                    }
                    val resolvedTarget = resolveTarget(targetType, target)
                    if (resolvedTarget == null) {
                        sender.sendMessage(
                            messages.msg(messages.command.player.error.targetNotFound, tags("target" to target)),
                        )
                        return@suspendingHandler
                    }
                    sendConnectResult(sender, player, resolvedTarget.name, connect(player, resolvedTarget))
                }
                .permission(Permission.permission(CommandPermissions.PLAYER_SEND))
                .build(),
        )
    }

    private fun sendConnectResult(sender: C, player: CloudPlayer, targetName: String, result: ConnectResult?) {
        val messages = plugin.messageConfiguration
        val playerTags = tags(
            "playername" to player.name,
            "displayname" to player.displayName,
            "target" to targetName,
        )
        val message = when (result) {
            ConnectResult.SUCCESS -> messages.msg(messages.command.player.send.success, playerTags)

            ConnectResult.ALREADY_CONNECTED -> messages.msg(messages.command.player.error.sameTarget, playerTags)

            ConnectResult.SERVER_NOT_FOUND ->
                messages.msg(messages.command.player.error.targetNotFound, tags("target" to targetName))

            ConnectResult.PLAYER_NOT_FOUND ->
                messages.msg(messages.command.player.error.notOnline, tags("playername" to player.name))

            else -> messages.msg(messages.command.player.error.sendFailed, playerTags)
        }
        sender.sendMessage(message)
    }

    private fun registerSendAll() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("player")
                .literal("sendall")
                .required("targetType", stringParser(), CloudSuggestions.targetTypes())
                .required("target", stringParser(), CloudSuggestions.targets(api))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val targetType = context.get<String>("targetType")
                    val target = context.get<String>("target")
                    val messages = plugin.messageConfiguration
                    if (!isTargetTypeValid(targetType)) {
                        sender.sendMessage(messages.msg(messages.command.usage.invalidTargetType))
                        return@suspendingHandler
                    }
                    val resolvedTarget = resolveTarget(targetType, target)
                    if (resolvedTarget == null) {
                        sender.sendMessage(
                            messages.msg(messages.command.player.error.targetNotFound, tags("target" to target)),
                        )
                        return@suspendingHandler
                    }
                    val players = api.player().onlinePlayers.await()
                    if (players.isEmpty()) {
                        sender.sendMessage(messages.msg(messages.command.player.error.noOnlinePlayers))
                        return@suspendingHandler
                    }
                    val results = connectAll(players, resolvedTarget)
                    val sent = results.count { it == ConnectResult.SUCCESS }
                    when {
                        sent > 0 -> sender.sendMessage(
                            messages.msg(
                                messages.command.player.send.successAll,
                                tags("count" to sent, "target" to resolvedTarget.name),
                            ),
                        )

                        results.all {
                            it == ConnectResult.ALREADY_CONNECTED
                        } -> sender.sendMessage(
                            messages.msg(messages.command.player.error.allOnTarget, tags("target" to resolvedTarget.name)),
                        )

                        else -> sender.sendMessage(
                            messages.msg(messages.command.player.error.sendAllFailed, tags("target" to resolvedTarget.name)),
                        )
                    }
                }
                .permission(Permission.permission(CommandPermissions.PLAYER_SEND))
                .build(),
        )
    }

    private fun registerSendFrom() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("player")
                .literal("sendfrom")
                .required("source", stringParser(), CloudSuggestions.servers(api))
                .required("targetType", stringParser(), CloudSuggestions.targetTypes())
                .required("target", stringParser(), CloudSuggestions.targets(api))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val source = context.get<String>("source")
                    val targetType = context.get<String>("targetType")
                    val target = context.get<String>("target")
                    val messages = plugin.messageConfiguration
                    if (!isTargetTypeValid(targetType)) {
                        sender.sendMessage(messages.msg(messages.command.usage.invalidTargetType))
                        return@suspendingHandler
                    }
                    val resolvedTarget = resolveTarget(targetType, target)
                    if (resolvedTarget == null) {
                        sender.sendMessage(
                            messages.msg(messages.command.player.error.targetNotFound, tags("target" to target)),
                        )
                        return@suspendingHandler
                    }
                    val sourceKey = source.lowercase()
                    val serverSources = api.server().allServers.await()
                        .filter {
                            sourceKey in serverIdentifiers(it) ||
                                (it.group?.name ?: it.serverGroupId).equals(source, true)
                        }
                        .map { serverIdentifiers(it) }
                    val persistentServerSources = api.persistentServer().allPersistentServers.await()
                        .map { serverIdentifiers(it) }
                        .filter { sourceKey in it }
                    val sourceIdentifiers = (serverSources + persistentServerSources).flatten().toSet()
                    if (sourceIdentifiers.isEmpty()) {
                        sender.sendMessage(
                            messages.msg(messages.command.player.error.sourceNotFound, tags("source" to source)),
                        )
                        return@suspendingHandler
                    }
                    val players = playersOn(sourceIdentifiers)
                    if (players.isEmpty()) {
                        sender.sendMessage(
                            messages.msg(messages.command.player.error.noSourcePlayers, tags("source" to source)),
                        )
                        return@suspendingHandler
                    }
                    val results = connectAll(players, resolvedTarget)
                    val sent = results.count { it == ConnectResult.SUCCESS }
                    when {
                        sent > 0 -> sender.sendMessage(
                            messages.msg(
                                messages.command.player.send.successFromServer,
                                tags("count" to sent, "source" to source, "target" to resolvedTarget.name),
                            ),
                        )

                        results.all {
                            it == ConnectResult.ALREADY_CONNECTED
                        } -> sender.sendMessage(
                            messages.msg(messages.command.player.error.allOnTarget, tags("target" to resolvedTarget.name)),
                        )

                        else -> sender.sendMessage(
                            messages.msg(messages.command.player.error.sendAllFailed, tags("target" to resolvedTarget.name)),
                        )
                    }
                }
                .permission(Permission.permission(CommandPermissions.PLAYER_SEND))
                .build(),
        )
    }

    private fun registerMessage() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("player")
                .literal("message")
                .required("player", stringParser(), CloudSuggestions.onlinePlayers(api))
                .required("message", stringParser(StringMode.GREEDY))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val playerName = context.get<String>("player")
                    val message = context.get<String>("message")
                    val messages = plugin.messageConfiguration
                    if (message.isBlank()) {
                        sender.sendMessage(
                            messages.msg(messages.command.player.error.messageEmpty, tags("playername" to playerName)),
                        )
                        return@suspendingHandler
                    }
                    val player = api.player().find(playerName).await().orElse(null)
                    if (player == null || !player.isOnline) {
                        sender.sendMessage(
                            messages.msg(messages.command.player.error.notOnline, tags("playername" to playerName)),
                        )
                        return@suspendingHandler
                    }
                    runCatching { player.sendMessage(Component.text(message)) }
                        .onSuccess {
                            sender.sendMessage(
                                messages.msg(
                                    messages.command.player.message.success,
                                    tags("playername" to player.name, "displayname" to player.displayName),
                                ),
                            )
                        }
                        .onFailure {
                            sender.sendMessage(
                                messages.msg(
                                    messages.command.player.error.messageFailed,
                                    tags("playername" to player.name),
                                ),
                            )
                        }
                }
                .permission(Permission.permission(CommandPermissions.PLAYER_MESSAGE))
                .build(),
        )
    }

    private fun isTargetTypeValid(targetType: String): Boolean =
        targetType.equals(TARGET_TYPE_GROUP, true) || targetType.equals(TARGET_TYPE_PERSISTENT_SERVER, true)

    private suspend fun resolveTarget(targetType: String, target: String): ServerBase? = when {
        targetType.equals(TARGET_TYPE_GROUP, true) -> ApiUtils.findGroup(api, target)
        targetType.equals(TARGET_TYPE_PERSISTENT_SERVER, true) -> ApiUtils.findPersistentServer(api, target)
        else -> null
    }

    private suspend fun playersForTarget(target: ServerBase): List<CloudPlayer> = when (target) {
        is Group -> playersOn(api.server().getServersByGroup(target.name).await().flatMap { serverIdentifiers(it) }.toSet())
        is PersistentServer -> playersOn(serverIdentifiers(target))
        else -> emptyList()
    }

    private suspend fun connect(player: CloudPlayer, target: ServerBase): ConnectResult = when (target) {
        is Group -> player.connectToGroup(target.name)
        is PersistentServer -> player.connectToPersistentServer(target.persistentServerId)
        else -> CompletableFuture.completedFuture(ConnectResult.SERVER_NOT_FOUND)
    }.await()

    private suspend fun playersOn(identifiers: Set<String>): List<CloudPlayer> =
        api.player().onlinePlayers.await().filter { player ->
            player.connectedServerName?.lowercase() in identifiers || player.connectedProxyName?.lowercase() in identifiers
        }

    private suspend fun connectAll(players: List<CloudPlayer>, target: ServerBase): List<ConnectResult?> = coroutineScope {
        players.map { player -> async { runCatching { connect(player, target) }.getOrNull() } }.awaitAll()
    }

    private fun serverIdentifiers(server: Server): Set<String> {
        val groupName = server.group?.name ?: server.serverGroupId
        val numbered = if (groupName.isNullOrBlank()) {
            emptyList()
        } else {
            listOf("$groupName-${server.numericalId}")
        }
        return (listOfNotNull(server.serverId, server.persistentServerId, server.persistentServer?.name) + numbered)
            .filter { it.isNotBlank() }
            .map { it.lowercase() }
            .toSet()
    }

    private fun serverIdentifiers(server: PersistentServer): Set<String> =
        listOfNotNull(server.persistentServerId, server.name)
            .filter { it.isNotBlank() }
            .map { it.lowercase() }
            .toSet()
}
