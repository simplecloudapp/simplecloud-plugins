package app.simplecloud.plugin.connection.shared.registration

import java.util.concurrent.ConcurrentHashMap

class TestServerRegistry : ServerRegistry {

    private val serversByName = ConcurrentHashMap<String, RegisteredServer>()

    override fun register(proxyName: String, server: RegisteredServer) {
        serversByName[proxyName.lowercase()] = server
    }

    override fun unregister(proxyName: String, server: RegisteredServer) {
        serversByName.remove(proxyName.lowercase())
    }

    override fun clear() {
        serversByName.clear()
    }

    fun findServer(proxyName: String): RegisteredServer? = serversByName[proxyName.lowercase()]
}
