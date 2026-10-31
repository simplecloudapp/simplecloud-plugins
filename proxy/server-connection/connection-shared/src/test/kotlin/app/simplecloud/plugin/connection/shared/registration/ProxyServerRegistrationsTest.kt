package app.simplecloud.plugin.connection.shared.registration

import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProxyServerRegistrationsTest {

    private val proxy = FakeProxyRegistry()
    private val registrations = ProxyServerRegistrations(proxy)

    @Test
    fun `maps a registered server ID to its proxy name`() {
        val lobby = registeredServer("lobby-id", "lobby-1")

        registrations.register(lobby)

        assertEquals(Optional.of("lobby-1"), registrations.proxyNameOf("lobby-id"))
        assertEquals(lobby, proxy.serverNamed("lobby-1"))
    }

    @Test
    fun `doesn't map a server that another server replaced under the same name`() {
        val replaced = registeredServer("replaced-id", "lobby-1")
        val holder = registeredServer("holder-id", "lobby-1")

        registrations.register(replaced)
        registrations.register(holder)

        assertEquals(Optional.empty(), registrations.proxyNameOf("replaced-id"))
        assertEquals(Optional.of("lobby-1"), registrations.proxyNameOf("holder-id"))
        assertEquals(holder, proxy.serverNamed("lobby-1"))
    }

    @Test
    fun `treats names that differ only in case as the same name`() {
        val replaced = registeredServer("replaced-id", "lobby-1")
        val holder = registeredServer("holder-id", "Lobby-1")

        registrations.register(replaced)
        registrations.register(holder)

        assertEquals(Optional.empty(), registrations.proxyNameOf("replaced-id"))
        assertEquals(Optional.of("Lobby-1"), registrations.proxyNameOf("holder-id"))
    }

    @Test
    fun `keeps the holder on the proxy when a replaced server is unregistered`() {
        val replaced = registeredServer("replaced-id", "lobby-1")
        val holder = registeredServer("holder-id", "Lobby-1")
        registrations.register(replaced)
        registrations.register(holder)

        registrations.unregister("replaced-id")

        assertEquals(holder, proxy.serverNamed("Lobby-1"))
        assertEquals(Optional.of("Lobby-1"), registrations.proxyNameOf("holder-id"))
    }

    @Test
    fun `leaves the proxy untouched when a replaced server is unregistered`() {
        val firstReplaced = registeredServer("first-replaced-id", "lobby-1")
        val secondReplaced = registeredServer("second-replaced-id", "lobby-1")
        val holder = registeredServer("holder-id", "lobby-1")
        registrations.register(firstReplaced)
        registrations.register(secondReplaced)
        registrations.register(holder)
        val changeCountBefore = proxy.changeCount

        registrations.unregister("first-replaced-id")

        assertEquals(changeCountBefore, proxy.changeCount)
        assertEquals(holder, proxy.serverNamed("lobby-1"))
    }

    @Test
    fun `hands the name to a replaced server when the holder is unregistered`() {
        val replaced = registeredServer("replaced-id", "lobby-1")
        val holder = registeredServer("holder-id", "lobby-1")
        registrations.register(replaced)
        registrations.register(holder)

        registrations.unregister("holder-id")

        assertEquals(replaced, proxy.serverNamed("lobby-1"))
        assertEquals(Optional.of("lobby-1"), registrations.proxyNameOf("replaced-id"))
        assertEquals(Optional.empty(), registrations.proxyNameOf("holder-id"))
    }

    @Test
    fun `removes the name from the proxy when its last server is unregistered`() {
        val lobby = registeredServer("lobby-id", "lobby-1")
        registrations.register(lobby)

        registrations.unregister("lobby-id")

        assertNull(proxy.serverNamed("lobby-1"))
        assertEquals(Optional.empty(), registrations.proxyNameOf("lobby-id"))
    }

    @Test
    fun `moves a server to its new proxy name`() {
        val lobby = registeredServer("lobby-id", "lobby-1")
        val renamedLobby = lobby.copy(proxyName = "hub-1")
        registrations.register(lobby)

        registrations.register(renamedLobby)

        assertNull(proxy.serverNamed("lobby-1"))
        assertEquals(renamedLobby, proxy.serverNamed("hub-1"))
        assertEquals(Optional.of("hub-1"), registrations.proxyNameOf("lobby-id"))
    }

    private fun registeredServer(serverId: String, proxyName: String): RegisteredServer = RegisteredServer(
        serverId = serverId,
        numericalId = 1,
        ip = "127.0.0.1",
        port = 25565,
        serverBaseName = "lobby",
        proxyName = proxyName,
        properties = emptyMap(),
        blueprintConfigurator = null,
        persistent = false,
    )
}
