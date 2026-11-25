package app.simplecloud.core.paper.command

import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

class PaperCloudSender(private val sourceStack: CommandSourceStack) : app.simplecloud.core.shared.command.CloudSender {

    fun getSourceStack(): CommandSourceStack = sourceStack

    fun getCommandSender(): CommandSender = sourceStack.sender

    override fun getUniqueId(): UUID? = (sourceStack.sender as? Player)?.uniqueId

    override fun hasPermission(permission: String): Boolean = sourceStack.sender.hasPermission(permission)

    override fun sendMessage(message: Component) {
        sourceStack.sender.sendMessage(message)
    }
}
