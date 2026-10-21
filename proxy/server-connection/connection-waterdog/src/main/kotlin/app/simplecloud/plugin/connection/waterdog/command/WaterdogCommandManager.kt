package app.simplecloud.plugin.connection.waterdog.command

import app.simplecloud.plugin.connection.shared.config.CommandEntry
import app.simplecloud.plugin.connection.shared.connection.CommandTarget
import app.simplecloud.plugin.connection.shared.connection.ConnectionResolver
import app.simplecloud.plugin.connection.waterdog.WaterdogConnectionPlugin
import app.simplecloud.plugin.connection.waterdog.platform.WaterdogConnectionPlayer
import dev.waterdog.waterdogpe.command.Command
import dev.waterdog.waterdogpe.command.CommandSender
import dev.waterdog.waterdogpe.command.CommandSettings
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo
import dev.waterdog.waterdogpe.player.ProxiedPlayer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList

class WaterdogCommandManager(
    private val plugin: WaterdogConnectionPlugin,
) {

    private val commands = CopyOnWriteArrayList<Command>()
    private val serializer = PlainTextComponentSerializer.plainText()
    private val logger = LoggerFactory.getLogger(WaterdogCommandManager::class.java)

    fun registerCommands() {
        val commands = plugin.connectionPlugin.commandConfig.get().commands
        for (command in commands) {
            registerCommand(command)
        }
    }

    fun unregisterCommands() {
        val commandMap = plugin.proxy.commandMap
        // Collect the names first, so the command name and every alias of our commands are removed.
        val registeredNames = commandMap.commands
            .filter { (_, command) -> command in commands }
            .map { (name, _) -> name }
        registeredNames.forEach { name -> commandMap.unregisterCommand(name) }
        commands.clear()
    }

    private fun registerCommand(command: CommandEntry) {
        val settingsBuilder = CommandSettings.builder()
            .setAliases(*command.aliases.toTypedArray())
        if (command.permission.isNotEmpty()) {
            settingsBuilder.permission = command.permission
        }
        val settings = settingsBuilder.build()

        val connectionCommand = object : Command(command.name, settings) {
            override fun onExecute(sender: CommandSender, alias: String?, args: Array<out String>): Boolean {
                val player = sender as? ProxiedPlayer ?: return false
                handleCommand(player, command)
                return true
            }
        }

        plugin.proxy.commandMap.registerCommand(connectionCommand)
        commands.add(connectionCommand)
    }

    private fun handleCommand(player: ProxiedPlayer, command: CommandEntry) {
        if (command.permission.isNotEmpty() && !player.hasPermission(command.permission)) return

        val config = plugin.connectionPlugin.connectionConfig.get()
        val target = ConnectionResolver.findCommandTarget(
            plugin.platform,
            WaterdogConnectionPlayer(player),
            command.targetConnections,
            config,
        )

        when (target) {
            is CommandTarget.Connect -> connect(player, target.server.serverInfo)
            CommandTarget.AlreadyConnected -> sendMessage(player, command.messages.alreadyConnected)
            CommandTarget.NotFound -> sendMessage(player, command.messages.noTargetConnectionFound)
        }
    }

    private fun connect(player: ProxiedPlayer, serverInfo: ServerInfo) {
        try {
            player.connect(serverInfo)
        } catch (exception: Exception) {
            logger.warn("Failed to send player ${player.name} to ${serverInfo.serverName}", exception)
        }
    }

    private fun sendMessage(player: ProxiedPlayer, rawMessage: String) {
        val messages = plugin.connectionPlugin.messageConfig.get()
        val plain = serializer.serialize(messages.msg(rawMessage))
        player.sendMessage(plain)
    }
}
