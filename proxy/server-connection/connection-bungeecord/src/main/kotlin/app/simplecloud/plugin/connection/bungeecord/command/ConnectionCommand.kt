package app.simplecloud.plugin.connection.bungeecord.command

import app.simplecloud.plugin.connection.bungeecord.BungeeCordConnectionPlugin
import app.simplecloud.plugin.connection.bungeecord.platform.BungeeCordConnectionPlayer
import app.simplecloud.plugin.connection.shared.config.CommandEntry
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command

class ConnectionCommand(
    private val plugin: BungeeCordConnectionPlugin,
    private val command: CommandEntry,
) : Command(command.name, command.permission.ifEmpty { null }, *command.aliases.toTypedArray()) {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        val player = sender as? ProxiedPlayer ?: return
        plugin.connectionPlugin.resolver.executeCommand(BungeeCordConnectionPlayer(player, plugin.proxy), command)
    }
}
