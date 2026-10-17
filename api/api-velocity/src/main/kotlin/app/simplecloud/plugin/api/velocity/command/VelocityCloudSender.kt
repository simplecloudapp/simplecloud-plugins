package app.simplecloud.plugin.api.velocity.command

import app.simplecloud.plugin.api.shared.command.CloudSender
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import java.util.UUID

class VelocityCloudSender(private val commandSource: CommandSource) : CloudSender {

    fun getCommandSource(): CommandSource {
        return commandSource
    }

    override fun getUniqueId(): UUID {
        return (commandSource as? Player)?.uniqueId ?: UUID(0L, 0L)
    }

    override fun hasPermission(permission: String): Boolean {
        return commandSource.hasPermission(permission)
    }

    override fun sendMessage(message: Component) {
        commandSource.sendMessage(message)
    }

}
