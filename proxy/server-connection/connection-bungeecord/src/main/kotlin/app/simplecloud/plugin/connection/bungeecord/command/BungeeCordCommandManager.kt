package app.simplecloud.plugin.connection.bungeecord.command

import app.simplecloud.plugin.connection.bungeecord.BungeeCordConnectionPlugin
import app.simplecloud.plugin.connection.bungeecord.platform.BungeeCordConnectionPlayer
import app.simplecloud.plugin.connection.shared.config.CommandEntry
import app.simplecloud.plugin.connection.shared.connection.CommandTarget
import app.simplecloud.plugin.connection.shared.connection.ConnectionResolver
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ServerConnectRequest
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.plugin.Command
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList

class BungeeCordCommandManager(
    private val plugin: BungeeCordConnectionPlugin,
    private val audiences: BungeeAudiences,
) {

    private val commands = CopyOnWriteArrayList<Command>()
    private val logger = LoggerFactory.getLogger(BungeeCordCommandManager::class.java)

    fun registerCommands() {
        val commands = plugin.connectionPlugin.commandConfig.get().commands
        for (command in commands) {
            registerCommand(command)
        }
    }

    fun unregisterCommands() {
        commands.forEach { plugin.proxy.pluginManager.unregisterCommand(it) }
        commands.clear()
    }

    private fun registerCommand(command: CommandEntry) {
        val permission = command.permission.ifEmpty { null }

        val connectionCommand = object : Command(
            command.name,
            permission,
            *command.aliases.toTypedArray(),
        ) {
            override fun execute(sender: CommandSender, args: Array<out String>) {
                val player = sender as? ProxiedPlayer ?: return
                handleCommand(player, command)
            }
        }

        plugin.proxy.pluginManager.registerCommand(plugin, connectionCommand)
        commands.add(connectionCommand)
    }

    private fun handleCommand(player: ProxiedPlayer, command: CommandEntry) {
        val config = plugin.connectionPlugin.connectionConfig.get()
        val messages = plugin.connectionPlugin.messageConfig.get()
        val audience = audiences.player(player)

        if (command.permission.isNotEmpty() && !player.hasPermission(command.permission)) return

        val target = ConnectionResolver.findCommandTarget(
            plugin.platform,
            BungeeCordConnectionPlayer(player),
            command.targetConnections,
            config,
        )

        when (target) {
            is CommandTarget.Connect -> connect(player, target.server.serverInfo)
            CommandTarget.AlreadyConnected -> audience.sendMessage(messages.msg(command.messages.alreadyConnected))
            CommandTarget.NotFound -> audience.sendMessage(messages.msg(command.messages.noTargetConnectionFound))
        }
    }

    private fun connect(player: ProxiedPlayer, targetServer: ServerInfo) {
        player.connect(
            ServerConnectRequest.builder()
                .target(targetServer)
                .reason(ServerConnectEvent.Reason.COMMAND)
                .callback { result: ServerConnectRequest.Result?, error: Throwable? ->
                    if (error != null) {
                        logger.warn("Failed to send player ${player.name} to ${targetServer.name}", error)
                        return@callback
                    }
                    if (result == ServerConnectRequest.Result.SUCCESS) return@callback

                    logger.warn("Could not send player ${player.name} to ${targetServer.name} ($result)")
                }
                .build(),
        )
    }
}
