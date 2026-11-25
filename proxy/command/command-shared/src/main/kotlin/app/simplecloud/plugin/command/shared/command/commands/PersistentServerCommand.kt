package app.simplecloud.plugin.command.shared.command.commands

import app.simplecloud.api.CloudApi
import app.simplecloud.plugin.command.shared.CommandPlugin
import app.simplecloud.plugin.command.shared.utilities.CommandPermissions
import app.simplecloud.plugin.command.shared.utilities.Formatter
import app.simplecloud.core.shared.adventure.tags
import app.simplecloud.core.shared.command.CloudSender
import app.simplecloud.core.shared.command.CloudSuggestions
import app.simplecloud.core.shared.controller.ApiUtils
import app.simplecloud.core.shared.controller.isStopped
import app.simplecloud.core.shared.pretty.PrettifyCase
import app.simplecloud.core.shared.pretty.StringPrettifier
import kotlinx.coroutines.future.*
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.permission.Permission

class PersistentServerCommand<C : CloudSender>(
    private val api: CloudApi,
    private val plugin: CommandPlugin,
    private val manager: CommandManager<C>,
) {

    fun register() {
        registerList()
        registerInfo()
    }

    private fun registerList() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("ps")
                .literal("list")
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val messages = plugin.config
                    val persistentServers = api.persistentServer().allPersistentServers.await()
                    if (persistentServers.isEmpty()) {
                        sender.sendMessage(messages.msg(messages.command.persistentServer.list.empty))
                        return@suspendingHandler
                    }
                    val runningServers = api.server().allServers.await()
                        .filter { it.persistentServerId != null && !it.isStopped() }
                        .associateBy { it.persistentServerId }
                    sender.sendMessage(messages.msg(messages.command.persistentServer.list.title, tags("count" to persistentServers.size)))
                    persistentServers.sortedBy { it.name.lowercase() }.forEach { persistentServer ->
                        val server = runningServers[persistentServer.persistentServerId]
                        val value = if (server == null) {
                            "offline"
                        } else {
                            "${server.playerCount ?: 0}/${server.maxPlayers}, ${server.state.name.lowercase()}"
                        }
                        sender.sendMessage(
                            messages.msg(
                                messages.command.persistentServer.list.entry,
                                tags("server" to persistentServer.name, "value" to value),
                            ),
                        )
                    }
                }
                .permission(Permission.permission(CommandPermissions.PERSISTENT_SERVER_LIST))
                .build(),
        )
    }

    private fun registerInfo() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("ps")
                .literal("info")
                .required("name", stringParser(), CloudSuggestions.persistentServers(api))
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val name = context.get<String>("name")
                    val messages = plugin.config
                    val persistentServer = ApiUtils.findPersistentServer(api, name)
                    if (persistentServer == null) {
                        sender.sendMessage(messages.msg(messages.command.persistentServer.error.notFound, tags("server" to name)))
                        return@suspendingHandler
                    }

                    val server = ApiUtils.findRunningServer(api, persistentServer)
                    val state = if (server == null) "Offline" else StringPrettifier.prettify(server.state.name, PrettifyCase.SENTENCE)
                    sender.sendMessage(messages.msg(messages.command.persistentServer.info.title, tags("server" to persistentServer.name)))
                    listOf<Pair<String, Any?>>(
                        "Type" to StringPrettifier.prettify(persistentServer.type.name, PrettifyCase.SENTENCE),
                        "State" to state,
                        "Active" to Formatter.yesNo(persistentServer.isActive()),
                        "Priority" to (persistentServer.priority ?: "-"),
                        "Tags" to persistentServer.tags.orEmpty().joinToString(", ").ifEmpty { "-" },
                        "Players" to "${persistentServer.playerCount}/${persistentServer.maxPlayers ?: "-"}",
                        "Memory" to "${persistentServer.minMemory}-${persistentServer.maxMemory} MB",
                        "Serverhost" to (persistentServer.serverhostId ?: "-"),
                        "Uptime" to if (server == null) "-" else Formatter.since(server.createdAt),
                    ).forEach { (key, value) ->
                        sender.sendMessage(messages.msg(messages.command.persistentServer.info.entry, tags("key" to key, "value" to value)))
                    }
                }
                .permission(Permission.permission(CommandPermissions.PERSISTENT_SERVER_INFO))
                .build(),
        )
    }

}
