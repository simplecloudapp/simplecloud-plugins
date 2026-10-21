package app.simplecloud.plugin.connection.shared.platform

interface ConnectionPlatform<S : ConnectionServer> {

    fun getServers(): List<S>
}
