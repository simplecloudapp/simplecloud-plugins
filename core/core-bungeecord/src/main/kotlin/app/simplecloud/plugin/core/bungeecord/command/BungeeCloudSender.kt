package app.simplecloud.plugin.core.bungeecord.command

import app.simplecloud.plugin.core.bungeecord.text.BungeeComponentConverter
import app.simplecloud.plugin.core.shared.command.CloudSender
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.UUID

class BungeeCloudSender(private val commandSender: CommandSender) : CloudSender {

    fun getCommandSender(): CommandSender = commandSender

    override fun getUniqueId(): UUID? = (commandSender as? ProxiedPlayer)?.uniqueId

    override fun hasPermission(permission: String): Boolean = commandSender.hasPermission(permission)

    override fun sendMessage(message: Component) {
        commandSender.sendMessage(BungeeComponentConverter.toBungee(message))
    }
}
