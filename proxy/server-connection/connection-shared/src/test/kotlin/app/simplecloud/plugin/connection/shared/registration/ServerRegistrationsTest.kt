package app.simplecloud.plugin.connection.shared.registration

import kotlin.jvm.optionals.getOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ServerRegistrationsTest {

    private val registry = TestServerRegistry()
    private val registrations = ServerRegistrations(registry)

    @Test
    fun `registers a new server under its proxy name`() {
        val server = server("a", "lobby-1")

        registrations.register(server)

        assertEquals(server, registry.findServer("lobby-1"))
        assertEquals(server, registrations.find("a"))
        assertEquals("lobby-1", registrations.proxyNameOf("a").getOrNull())
    }

    @Test
    fun `a second server with the same name takes over the name`() {
        val first = server("a", "lobby-1")
        val second = server("b", "lobby-1")

        registrations.register(first)
        registrations.register(second)

        assertEquals(second, registry.findServer("lobby-1"))
        assertNull(registrations.proxyNameOf("a").getOrNull())
        assertEquals("lobby-1", registrations.proxyNameOf("b").getOrNull())
    }

    @Test
    fun `the name is handed over when the holder is unregistered`() {
        val first = server("a", "lobby-1")
        registrations.register(first)
        registrations.register(server("b", "lobby-1"))

        registrations.unregister("b")

        assertEquals(first, registry.findServer("lobby-1"))
        assertEquals("lobby-1", registrations.proxyNameOf("a").getOrNull())
    }

    @Test
    fun `the name is removed from the proxy when the last server is unregistered`() {
        registrations.register(server("a", "lobby-1"))

        registrations.unregister("a")

        assertNull(registry.findServer("lobby-1"))
        assertNull(registrations.find("a"))
    }

    @Test
    fun `unregistering a server that does not hold the name keeps the holder`() {
        registrations.register(server("a", "lobby-1"))
        val second = server("b", "lobby-1")
        registrations.register(second)

        registrations.unregister("a")

        assertEquals(second, registry.findServer("lobby-1"))
    }

    @Test
    fun `renaming a server frees its old name`() {
        registrations.register(server("a", "lobby-1"))
        val renamed = server("a", "lobby-x")

        registrations.register(renamed)

        assertNull(registry.findServer("lobby-1"))
        assertEquals(renamed, registry.findServer("lobby-x"))
    }

    @Test
    fun `names are compared ignoring case`() {
        registrations.register(server("a", "Lobby-1"))
        registrations.register(server("b", "lobby-1"))

        assertNull(registrations.proxyNameOf("a").getOrNull())
        assertEquals("lobby-1", registrations.proxyNameOf("b").getOrNull())
    }

    @Test
    fun `refreshing a server whose name is held by another server keeps the holder`() {
        registrations.register(server("a", "lobby-1"))
        val holder = server("b", "lobby-1")
        registrations.register(holder)
        val changed = server("a", "lobby-1").copy(port = 25570)

        registrations.register(changed)

        assertEquals(holder, registry.findServer("lobby-1"))
        assertEquals(changed, registrations.find("a"))
    }

    @Test
    fun `unregistering an unknown server does nothing`() {
        val server = server("a", "lobby-1")
        registrations.register(server)

        registrations.unregister("unknown")

        assertEquals(server, registry.findServer("lobby-1"))
    }

    private fun server(serverId: String, proxyName: String): RegisteredServer = RegisteredServer(
        serverId = serverId,
        numericalId = 1,
        ip = "127.0.0.1",
        port = 25565,
        serverBaseName = "lobby",
        proxyName = proxyName,
        properties = emptyMap(),
        persistent = false,
    )
}
