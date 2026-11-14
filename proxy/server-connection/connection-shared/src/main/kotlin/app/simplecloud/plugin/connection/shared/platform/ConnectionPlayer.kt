package app.simplecloud.plugin.connection.shared.platform

import net.kyori.adventure.text.Component

interface ConnectionPlayer {

    fun hasPermission(permission: String): Boolean

    fun getVirtualHost(): String?

    fun getServerName(): String?

    fun sendMessage(message: Component)

    fun connect(serverName: String)
}
