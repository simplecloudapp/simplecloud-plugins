package app.simplecloud.plugin.notify.shared.command

import net.kyori.adventure.text.Component
import java.util.UUID

interface CloudSender {

    fun getUniqueId(): UUID?

    fun sendMessage(message: Component)

    fun hasPermission(permission: String): Boolean
}
