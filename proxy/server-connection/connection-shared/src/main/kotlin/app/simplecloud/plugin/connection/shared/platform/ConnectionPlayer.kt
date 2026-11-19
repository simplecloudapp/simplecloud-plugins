package app.simplecloud.plugin.connection.shared.platform

import java.util.UUID

interface ConnectionPlayer {

    fun getUniqueId(): UUID

    fun hasPermission(permission: String): Boolean

    fun getVirtualHost(): String?

    fun getServerName(): String?
}
