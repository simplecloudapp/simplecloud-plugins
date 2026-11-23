package app.simplecloud.plugin.connection.bungeecord.command

import app.simplecloud.plugin.connection.bungeecord.BungeeCordConnectionPlugin
import app.simplecloud.plugin.connection.shared.command.ConnectionCommandManager
import app.simplecloud.plugin.connection.shared.config.CommandEntry
import net.md_5.bungee.api.plugin.Command
import java.util.concurrent.CopyOnWriteArrayList

class BungeeCordCommandManager(
    private val plugin: BungeeCordConnectionPlugin,
) : ConnectionCommandManager {

    private val registeredCommands = CopyOnWriteArrayList<Command>()

    override fun register(commands: List<CommandEntry>) {
        commands.forEach { command -> register(command) }
    }

    override fun unregister() {
        registeredCommands.forEach { command -> plugin.proxy.pluginManager.unregisterCommand(command) }
        registeredCommands.clear()
    }

    private fun register(command: CommandEntry) {
        val connectionCommand = ConnectionCommand(plugin, command)
        plugin.proxy.pluginManager.registerCommand(plugin, connectionCommand)
        registeredCommands.add(connectionCommand)
    }
}
