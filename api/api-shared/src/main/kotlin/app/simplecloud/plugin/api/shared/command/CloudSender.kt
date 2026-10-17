package app.simplecloud.plugin.api.shared.command

import net.kyori.adventure.text.Component
import java.util.UUID

interface CloudSender {

    fun getUniqueId(): UUID

    fun hasPermission(permission: String): Boolean

    fun sendMessage(message: Component)

}