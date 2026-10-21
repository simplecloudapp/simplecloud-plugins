package app.simplecloud.plugin.connection.shared.platform

interface ConnectionServer {

    fun getName(): String

    fun getPlayerCount(): Int
}
