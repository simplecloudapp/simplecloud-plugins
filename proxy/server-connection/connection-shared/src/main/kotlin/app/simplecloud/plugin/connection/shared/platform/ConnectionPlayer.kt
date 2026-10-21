package app.simplecloud.plugin.connection.shared.platform

interface ConnectionPlayer {

    fun hasPermission(permission: String): Boolean

    fun getVirtualHost(): String?
}
