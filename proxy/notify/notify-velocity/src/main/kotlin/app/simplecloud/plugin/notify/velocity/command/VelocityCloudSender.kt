package app.simplecloud.plugin.notify.velocity.command

import app.simplecloud.plugin.notify.shared.command.CloudSender
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import java.util.UUID

class VelocityCloudSender(
    val source: CommandSource,
) : CloudSender {

    override fun getUniqueId(): UUID? {
        return (source as? Player)?.uniqueId
    }

    override fun sendMessage(message: Component) {
        source.sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return source.hasPermission(permission)
    }
}