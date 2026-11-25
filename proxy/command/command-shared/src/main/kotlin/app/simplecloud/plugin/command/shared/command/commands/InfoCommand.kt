package app.simplecloud.plugin.command.shared.command.commands

import app.simplecloud.api.CloudApi
import app.simplecloud.plugin.command.shared.CommandPlugin
import app.simplecloud.plugin.command.shared.utilities.CommandPermissions
import app.simplecloud.core.shared.adventure.tags
import app.simplecloud.core.shared.command.CloudSender
import app.simplecloud.core.shared.pretty.PrettifyCase
import app.simplecloud.core.shared.pretty.StringPrettifier
import kotlinx.coroutines.future.*
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.permission.Permission

class InfoCommand<C : CloudSender>(
    private val api: CloudApi,
    private val plugin: CommandPlugin,
    private val manager: CommandManager<C>,
) {

    fun register() {
        manager.command(
            manager.commandBuilder("cloud", "sc", "simplecloud")
                .literal("info")
                .suspendingHandler(plugin.scope) { context ->
                    val sender = context.sender()
                    val messages = plugin.config
                    val stats = api.stats().stats.await()
                    val servers = stats.servers()
                    val states = servers.byState()
                    val players = stats.players()
                    val groups = stats.serverGroups()
                    val persistentServers = stats.persistentServers()
                    val serverhosts = stats.serverhosts()
                    val resources = stats.resources()
                    val health = stats.health()
                    val healthStatus = StringPrettifier.prettify(health.status() ?: "unknown", PrettifyCase.SENTENCE)
                    sender.sendMessage(messages.msg(messages.command.info.title))
                    listOf(
                        "Health" to "$healthStatus (${percent(health.healthyServerPercent())} of servers healthy)",
                        "Players" to "${players.online()}/${players.capacity()} (${percent(players.utilizationPercent())})",
                        "Servers" to "${servers.active()} active, ${servers.total()} total",
                        "States" to "${states.queued()} queued, ${states.preparing()} preparing, ${states.starting()} starting, " +
                            "${states.available()} available, ${states.ingame()} ingame, ${states.stopping()} stopping",
                        "Groups" to "${groups.active()} active, ${groups.total()} total",
                        "Persistent servers" to "${persistentServers.active()} active, ${persistentServers.total()} total",
                        "Serverhosts" to "${serverhosts.active()} active, ${serverhosts.total()} total",
                        "Memory" to "${gigabytes(resources.memoryUsedMb())}/${gigabytes(serverhosts.totalMemoryCapacityMb().toDouble())} GB used, " +
                            "${gigabytes(resources.memoryAllocatedMb().toDouble())} GB allocated",
                        "CPU" to "${percent(resources.averageCpuPercent())} average",
                    ).forEach { (key, value) ->
                        sender.sendMessage(messages.msg(messages.command.info.entry, tags("key" to key, "value" to value)))
                    }
                }
                .permission(Permission.permission(CommandPermissions.INFO))
                .build(),
        )
    }

    private fun percent(value: Double): String = "%.1f%%".format(value)

    private fun gigabytes(megabytes: Double): String = "%.1f".format(megabytes / 1024.0)
}
