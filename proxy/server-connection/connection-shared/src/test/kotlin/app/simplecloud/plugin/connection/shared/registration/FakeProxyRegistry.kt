package app.simplecloud.plugin.connection.shared.registration

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Keeps one server per name and compares names ignoring case, like Velocity and BungeeCord. Counts every change, so
 * tests can check that the proxy was left untouched.
 */
class FakeProxyRegistry : ServerRegistry {

    private val serversByName = ConcurrentHashMap<String, RegisteredServer>()
    private val changes = AtomicInteger()

    val changeCount: Int
        get() = changes.get()

    override fun register(proxyName: String, server: RegisteredServer) {
        serversByName[proxyName.lowercase()] = server
        changes.incrementAndGet()
    }

    override fun unregister(proxyName: String, server: RegisteredServer) {
        serversByName.remove(proxyName.lowercase())
        changes.incrementAndGet()
    }

    fun serverNamed(proxyName: String): RegisteredServer? = serversByName[proxyName.lowercase()]
}
