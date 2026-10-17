package app.simplecloud.plugin.notify.bungeecord.command

import app.simplecloud.plugin.notify.shared.command.CloudSender
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.UUID

class BungeeCloudSender(
    val source: CommandSender,
    private val adventure: BungeeAudiences,
) : CloudSender {

    override fun getUniqueId(): UUID? {
        return (source as? ProxiedPlayer)?.uniqueId
    }

    override fun sendMessage(message: Component) {
        adventure.sender(source).sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return source.hasPermission(permission)
    }
}