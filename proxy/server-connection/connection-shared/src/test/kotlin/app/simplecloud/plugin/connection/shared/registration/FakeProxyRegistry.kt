package app.simplecloud.plugin.connection.shared.registration

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * Keeps one server per name and compares names ignoring case, like the proxies. Counts every change, so tests can
 * check that the proxy was left untouched, and runs the listeners added with [beforeRegister] just before it switches
 * a name over to a server.
 */
class FakeProxyRegistry : ServerRegistry {

    private val serversByName = ConcurrentHashMap<String, RegisteredServer>()
    private val changes = AtomicInteger()
    private val beforeRegisterListeners = CopyOnWriteArrayList<(RegisteredServer) -> Unit>()

    val changeCount: Int
        get() = changes.get()

    override fun register(proxyName: String, server: RegisteredServer) {
        beforeRegisterListeners.forEach { listener -> listener(server) }
        serversByName[proxyName.lowercase()] = server
        changes.incrementAndGet()
    }

    override fun unregister(proxyName: String, server: RegisteredServer) {
        serversByName.remove(proxyName.lowercase())
        changes.incrementAndGet()
    }

    fun serverNamed(proxyName: String): RegisteredServer? = serversByName[proxyName.lowercase()]

    fun beforeRegister(listener: (RegisteredServer) -> Unit) {
        beforeRegisterListeners.add(listener)
    }
}
