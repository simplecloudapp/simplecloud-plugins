package app.simplecloud.plugin.core.velocity.command

import app.simplecloud.plugin.core.shared.command.CloudSender
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import java.util.UUID

class VelocityCloudSender(private val commandSource: CommandSource) : CloudSender {

    fun getCommandSource(): CommandSource = commandSource

    override fun getUniqueId(): UUID? = (commandSource as? Player)?.uniqueId

    override fun hasPermission(permission: String): Boolean = commandSource.hasPermission(permission)

    override fun sendMessage(message: Component) {
        commandSource.sendMessage(message)
    }
}
