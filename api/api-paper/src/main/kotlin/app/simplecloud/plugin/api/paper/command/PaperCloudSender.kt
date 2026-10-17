package app.simplecloud.plugin.api.paper.command

import app.simplecloud.plugin.api.shared.command.CloudSender
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

class PaperCloudSender(private val sourceStack: CommandSourceStack) : CloudSender {

    fun getSourceStack(): CommandSourceStack {
        return sourceStack
    }

    fun getCommandSender(): CommandSender {
        return sourceStack.sender
    }

    override fun getUniqueId(): UUID {
        return (sourceStack.sender as? Player)?.uniqueId ?: UUID(0L, 0L)
    }

    override fun hasPermission(permission: String): Boolean {
        return sourceStack.sender.hasPermission(permission)
    }

    override fun sendMessage(message: Component) {
        sourceStack.sender.sendMessage(message)
    }

}
