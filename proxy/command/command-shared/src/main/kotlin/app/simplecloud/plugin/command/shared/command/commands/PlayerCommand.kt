package app.simplecloud.plugin.command.shared.command.commands

import app.simplecloud.api.CloudApi
import app.simplecloud.api.group.Group
import app.simplecloud.api.player.CloudPlayer
import app.simplecloud.api.player.CloudPlayer.ConnectResult
import app.simplecloud.api.player.ServerSelectionMode
import app.simplecloud.core.shared.adventure.miniMessage
import app.simplecloud.core.shared.adventure.tags
import app.simplecloud.core.shared.command.CloudSender
import app.simplecloud.core.shared.command.CloudSuggestions
import app.simplecloud.core.shared.command.ListPage
import app.simplecloud.core.shared.command.CloudSuggestions.TARGET_TYPE_GROUP
import app.simplecloud.core.shared.command.CloudSuggestions.TARGET_TYPE_PERSISTENT_SERVER
import app.simplecloud.core.shared.command.CloudSuggestions.TARGET_TYPE_SERVER
import app.simplecloud.core.shared.controller.ApiUtils
import app.simplecloud.core.shared.controller.getIdentifiers
import app.simplecloud.plugin.command.shared.CommandPlugin
import app.simplecloud.plugin.command.shared.utilities.CommandPermissions
import app.simplecloud.plugin.command.shared.utilities.Formatter
import kotlinx.coroutines.future.*
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.parser.standard.EnumParser.enumParser
import org.incendo.cloud.parser.standard.IntegerParser.integerParser
import org.incendo.cloud.parser.standard.StringParser.StringMode
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.permission.Permission
import kotlin.time.Duration.Companion.seconds

class PlayerCommand<C : CloudSender>(
    private val api: CloudApi,
    private val plugin: CommandPlugin,
    private val manager: CommandManager<C>,
) {

    private val types = listOf(TARGET_TYPE_GROUP, TARGET_TYPE_PERSISTENT_SERVER, TARGET_TYPE_SERVER)

    fun register() {
        registerList()
        registerInfo()
        registerSend()
        registerSendAll()
        registerSendFrom()
        registerMessage()
        registerKick()
    }

    private fun registerList() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("player")
                .literal("list")
                .optional("targetType", stringParser(), CloudSuggestions.of(types))
                .optional("target", stringParser(), CloudSuggestions.targets(api))
                .optional("page", integerParser(1))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val typeArgument = context.getOrDefault("targetType", null as String?)
                    val pageArgument = typeArgument?.toIntOrNull()
                    val targetType = if (pageArgument == null) typeArgument else null
                    val target = context.getOrDefault("target", null as String?)
                    val requestedPage = pageArgument ?: context.getOrDefault("page", 1)
                    val messages = plugin.config
                    if ((targetType == null) != (target == null)) {
                        sender.sendMessage(messages.msg(messages.command.exception.syntax, tags("command" to "/cloud player list [<group|ps|server> <target>] [page]")))
                        return@suspendingHandler
                    }
                    val players = if (targetType == null || target == null) {
                        api.player().onlinePlayers.await()
                    } else {
                        val resolvedTarget = findTarget(sender, targetType, target, null) ?: return@suspendingHandler
                        ApiUtils.getPlayersOnTarget(api, resolvedTarget)
                    }
                    if (players.isEmpty()) {
                        sender.sendMessage(messages.msg(messages.command.player.list.empty))
                        return@suspendingHandler
                    }
                    val pages = ListPage.count(players.size)
                    val page = requestedPage.coerceIn(1, pages)
                    sender.sendMessage(messages.msg(messages.command.player.list.title, tags("count" to players.size)))
                    ListPage.entries(players.sortedBy { it.name.lowercase() }, page).forEach { player ->
                        sender.sendMessage(
                            messages.msg(
                                messages.command.player.list.entry,
                                tags(
                                    "playername" to player.name,
                                    "displayname" to player.displayName,
                                    "value" to (player.connectedServerName ?: "-"),
                                ),
                            ),
                        )
                    }
                    if (pages > 1) {
                        val command = listOfNotNull("/cloud player list", targetType, target, "<page>").joinToString(" ")
                        sender.sendMessage(ListPage.footer(messages, messages.command.page, page, pages, command))
                    }
                }
                .permission(Permission.permission(CommandPermissions.PLAYER_LIST))
                .build(),
        )
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
                    val messages = plugin.config
                    val player = ApiUtils.findPlayer(api, playerName)
                    if (player == null) {
                        sender.sendMessage(
                            messages.msg(messages.command.player.error.notFound, tags("playername" to playerName)),
                        )
                        return@suspendingHandler
                    }
                    val session = if (player.isOnline) api.player().findCurrentSession(player.uniqueId).await().orElse(null) else null
                    sender.sendMessage(
                        messages.msg(
                            messages.command.player.info.title,
                            tags("playername" to player.name, "displayname" to player.displayName),
                        ),
                    )
                    listOf<Pair<String, Any?>>(
                        "Name" to player.name,
                        "UUID" to player.uniqueId,
                        "Display name" to player.displayName,
                        "Online" to Formatter.yesNo(player.isOnline),
                        "Server" to (player.connectedServerName ?: "-"),
                        "Proxy" to (player.connectedProxyName ?: "-"),
                        "Online time" to player.onlineTimeSeconds.seconds,
                        "First seen" to Formatter.date(player.firstSeen),
                        "Last seen" to Formatter.date(player.lastSeen),
                        "Session" to if (session == null) "-" else "${Formatter.since(session.loginTime())} since ${Formatter.date(session.loginTime())}",
                        "Protocol" to (session?.clientVersion() ?: "-"),
                        "Online mode" to Formatter.yesNo(session?.onlineMode()),
                        "Language" to (session?.clientSettings()?.locale() ?: "-"),
                    ).forEach { (key, value) ->
                        sender.sendMessage(messages.msg(messages.command.player.info.entry, tags("key" to key, "value" to value)))
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
                .required("targetType", stringParser(), CloudSuggestions.of(types))
                .required("target", stringParser(), CloudSuggestions.targets(api))
                .optional("mode", enumParser(ServerSelectionMode::class.java))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val playerName = context.get<String>("player")
                    val mode = context.getOrDefault("mode", null as ServerSelectionMode?)
                    val messages = plugin.config
                    val player = ApiUtils.findPlayer(api, playerName)
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
                    val target = findTarget(sender, context.get("targetType"), context.get("target"), mode)
                        ?: return@suspendingHandler
                    sendConnectResult(sender, player, ApiUtils.getTargetName(target), ApiUtils.connect(player, target, mode))
                }
                .permission(Permission.permission(CommandPermissions.PLAYER_SEND))
                .build(),
        )
    }

    private fun sendConnectResult(sender: C, player: CloudPlayer, targetName: String, result: ConnectResult) {
        val messages = plugin.config
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
                .required("targetType", stringParser(), CloudSuggestions.of(types))
                .required("target", stringParser(), CloudSuggestions.targets(api))
                .optional("mode", enumParser(ServerSelectionMode::class.java))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val mode = context.getOrDefault("mode", null as ServerSelectionMode?)
                    val messages = plugin.config
                    val target = findTarget(sender, context.get("targetType"), context.get("target"), mode)
                        ?: return@suspendingHandler
                    val players = api.player().onlinePlayers.await()
                    if (players.isEmpty()) {
                        sender.sendMessage(messages.msg(messages.command.player.error.noOnlinePlayers))
                        return@suspendingHandler
                    }
                    val results = ApiUtils.connectAll(players, target, mode)
                    sendConnectResults(sender, results, ApiUtils.getTargetName(target), messages.command.player.send.successAll, "")
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
                .required("targetType", stringParser(), CloudSuggestions.of(types))
                .required("target", stringParser(), CloudSuggestions.targets(api))
                .optional("mode", enumParser(ServerSelectionMode::class.java))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val mode = context.getOrDefault("mode", null as ServerSelectionMode?)
                    val target = findTarget(sender, context.get("targetType"), context.get("target"), mode)
                        ?: return@suspendingHandler
                    sendFrom(sender, context.get("source"), target, mode)
                }
                .permission(Permission.permission(CommandPermissions.PLAYER_SEND))
                .build(),
        )
    }

    private suspend fun sendFrom(sender: C, source: String, target: Any, mode: ServerSelectionMode?) {
        val messages = plugin.config
        val sourceKey = source.lowercase()
        val serverSources = api.server().allServers.await()
            .filter { sourceKey in it.getIdentifiers() || (it.group?.name ?: it.serverGroupId).equals(source, true) }
            .map { it.getIdentifiers() }
        val persistentServerSources = api.persistentServer().allPersistentServers.await()
            .map { it.getIdentifiers() }
            .filter { sourceKey in it }
        val sourceIdentifiers = (serverSources + persistentServerSources).flatten().toSet()
        if (sourceIdentifiers.isEmpty()) {
            sender.sendMessage(messages.msg(messages.command.player.error.sourceNotFound, tags("source" to source)))
            return
        }
        val players = ApiUtils.getPlayersOn(api, sourceIdentifiers)
        if (players.isEmpty()) {
            sender.sendMessage(messages.msg(messages.command.player.error.noSourcePlayers, tags("source" to source)))
            return
        }
        val results = ApiUtils.connectAll(players, target, mode)
        sendConnectResults(sender, results, ApiUtils.getTargetName(target), messages.command.player.send.successFromServer, source)
    }

    private fun sendConnectResults(sender: C, results: List<ConnectResult?>, targetName: String, success: String, source: String) {
        val messages = plugin.config
        val sent = results.count { it == ConnectResult.SUCCESS }
        val message = when {
            sent > 0 -> messages.msg(success, tags("count" to sent, "source" to source, "target" to targetName))
            results.all { it == ConnectResult.ALREADY_CONNECTED } ->
                messages.msg(messages.command.player.error.allOnTarget, tags("target" to targetName))
            else -> messages.msg(messages.command.player.error.sendAllFailed, tags("target" to targetName))
        }
        sender.sendMessage(message)
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
                    val messages = plugin.config
                    if (message.isBlank()) {
                        sender.sendMessage(
                            messages.msg(messages.command.player.error.messageEmpty, tags("playername" to playerName)),
                        )
                        return@suspendingHandler
                    }
                    val player = ApiUtils.findPlayer(api, playerName)
                    if (player == null || !player.isOnline) {
                        sender.sendMessage(
                            messages.msg(messages.command.player.error.notOnline, tags("playername" to playerName)),
                        )
                        return@suspendingHandler
                    }
                    player.sendMessage(miniMessage.deserialize(message))
                    sender.sendMessage(
                        messages.msg(
                            messages.command.player.message.success,
                            tags("playername" to player.name, "displayname" to player.displayName),
                        ),
                    )
                }
                .permission(Permission.permission(CommandPermissions.PLAYER_MESSAGE))
                .build(),
        )
    }

    private fun registerKick() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("player")
                .literal("kick")
                .required("player", stringParser(), CloudSuggestions.onlinePlayers(api))
                .optional("reason", stringParser(StringMode.GREEDY))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val playerName = context.get<String>("player")
                    val reason = context.getOrDefault("reason", null as String?)
                    val messages = plugin.config
                    val player = ApiUtils.findPlayer(api, playerName)
                    if (player == null || !player.isOnline) {
                        sender.sendMessage(messages.msg(messages.command.player.error.notOnline, tags("playername" to playerName)))
                        return@suspendingHandler
                    }
                    val kickMessage = if (reason.isNullOrBlank()) null else miniMessage.deserialize(reason)
                    if (!api.player().kick(player.uniqueId, kickMessage).await()) {
                        sender.sendMessage(messages.msg(messages.command.player.error.notOnline, tags("playername" to player.name)))
                        return@suspendingHandler
                    }
                    sender.sendMessage(
                        messages.msg(
                            messages.command.player.kick.success,
                            tags("playername" to player.name, "displayname" to player.displayName),
                        ),
                    )
                }
                .permission(Permission.permission(CommandPermissions.PLAYER_KICK))
                .build(),
        )
    }

    private suspend fun findTarget(sender: C, targetType: String, target: String, mode: ServerSelectionMode?): Any? {
        val messages = plugin.config
        if (types.none { it.equals(targetType, true) }) {
            sender.sendMessage(messages.msg(messages.command.usage.invalidTargetType))
            return null
        }
        val resolvedTarget = ApiUtils.findTarget(api, targetType, target)
        return when {
            resolvedTarget == null -> {
                sender.sendMessage(messages.msg(messages.command.player.error.targetNotFound, tags("target" to target)))
                null
            }

            mode != null && resolvedTarget !is Group -> {
                sender.sendMessage(messages.msg(messages.command.usage.modeOnlyForGroups))
                null
            }

            else -> resolvedTarget
        }
    }
}
