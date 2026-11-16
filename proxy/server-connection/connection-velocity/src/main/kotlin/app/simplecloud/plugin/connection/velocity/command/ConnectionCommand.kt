package app.simplecloud.plugin.connection.velocity.command

import app.simplecloud.plugin.connection.shared.config.CommandEntry
import app.simplecloud.plugin.connection.velocity.VelocityConnectionPlugin
import app.simplecloud.plugin.connection.velocity.platform.VelocityConnectionPlayer
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer

class ConnectionCommand(
    private val plugin: VelocityConnectionPlugin,
    private val proxy: ProxyServer,
    private val command: CommandEntry,
) : SimpleCommand {

    override fun execute(invocation: SimpleCommand.Invocation) {
        val player = invocation.source() as? Player ?: return
        plugin.connectionPlugin.resolver.executeCommand(VelocityConnectionPlayer(player, proxy), command)
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean =
        command.permission.isEmpty() || invocation.source().hasPermission(command.permission)
}
