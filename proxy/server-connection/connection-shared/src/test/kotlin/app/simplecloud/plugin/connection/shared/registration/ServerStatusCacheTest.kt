package app.simplecloud.plugin.connection.shared.registration

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServerStatusCacheTest {

    private val registrations = ServerRegistrations(TestServerRegistry())
    private val statusCache = ServerStatusCache(registrations)

    @Test
    fun `statuses are found by proxy name`() {
        registrations.register(server("a", "lobby-1"))
        statusCache.update("a", ServerStatus(false, 7, 20))

        assertEquals(mapOf("lobby-1" to ServerStatus(false, 7, 20)), statusCache.findAllByProxyName())
    }

    @Test
    fun `registered servers without a status count as running and empty`() {
        registrations.register(server("a", "lobby-1"))

        assertEquals(ServerStatus(false, 0, 0), statusCache.findAllByProxyName()["lobby-1"])
    }

    @Test
    fun `statuses of unregistered servers are not returned`() {
        statusCache.update("a", ServerStatus(false, 7, 20))

        assertTrue(statusCache.findAllByProxyName().isEmpty())
    }

    @Test
    fun `only the status of the server holding the name is used`() {
        registrations.register(server("a", "lobby-1"))
        registrations.register(server("b", "lobby-1"))
        statusCache.update("a", ServerStatus(true, 3, 20))
        statusCache.update("b", ServerStatus(false, 1, 20))

        assertEquals(ServerStatus(false, 1, 20), statusCache.findAllByProxyName()["lobby-1"])
    }

    @Test
    fun `mark stopping keeps the player counts`() {
        registrations.register(server("a", "lobby-1"))
        statusCache.update("a", ServerStatus(false, 7, 20))

        statusCache.markStopping("a")

        assertEquals(ServerStatus(true, 7, 20), statusCache.findAllByProxyName()["lobby-1"])
    }

    @Test
    fun `mark stopping ignores servers without a status`() {
        registrations.register(server("a", "lobby-1"))

        statusCache.markStopping("a")

        assertEquals(ServerStatus(false, 0, 0), statusCache.findAllByProxyName()["lobby-1"])
    }

    @Test
    fun `removed statuses fall back to the default`() {
        registrations.register(server("a", "lobby-1"))
        statusCache.update("a", ServerStatus(true, 7, 20))

        statusCache.remove("a")

        assertEquals(ServerStatus(false, 0, 0), statusCache.findAllByProxyName()["lobby-1"])
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
