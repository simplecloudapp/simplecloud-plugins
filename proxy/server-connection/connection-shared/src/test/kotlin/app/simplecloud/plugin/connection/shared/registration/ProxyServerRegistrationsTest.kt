package app.simplecloud.plugin.connection.shared.registration

import java.util.Optional
import java.util.concurrent.CopyOnWriteArrayList
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

        assertEquals(Optional.of(lobby.proxyName), registrations.proxyNameOf(lobby.serverId))
        assertEquals(lobby, proxy.serverNamed(lobby.proxyName))
    }

    @Test
    fun `doesn't map a server that another server replaced under the same name`() {
        val replaced = registeredServer("replaced-id", "lobby-1")
        val holder = registeredServer("holder-id", "lobby-1")

        registrations.register(replaced)
        registrations.register(holder)

        assertEquals(Optional.empty(), registrations.proxyNameOf(replaced.serverId))
        assertEquals(Optional.of(holder.proxyName), registrations.proxyNameOf(holder.serverId))
        assertEquals(holder, proxy.serverNamed(holder.proxyName))
    }

    @Test
    fun `treats names that differ only in case as the same name`() {
        val replaced = registeredServer("replaced-id", "lobby-1")
        val holder = registeredServer("holder-id", "Lobby-1")

        registrations.register(replaced)
        registrations.register(holder)

        assertEquals(Optional.empty(), registrations.proxyNameOf(replaced.serverId))
        assertEquals(Optional.of(holder.proxyName), registrations.proxyNameOf(holder.serverId))
    }

    @Test
    fun `keeps the holder when a replaced server whose name differs only in case is unregistered`() {
        val replaced = registeredServer("replaced-id", "lobby-1")
        val holder = registeredServer("holder-id", "Lobby-1")
        registrations.register(replaced)
        registrations.register(holder)

        registrations.unregister(replaced.serverId)

        assertEquals(holder, proxy.serverNamed(holder.proxyName))
        assertEquals(Optional.of(holder.proxyName), registrations.proxyNameOf(holder.serverId))
    }

    @Test
    fun `leaves the proxy untouched when a replaced server is unregistered`() {
        val replaced = registeredServer("replaced-id", "lobby-1")
        val holder = registeredServer("holder-id", "lobby-1")
        registrations.register(replaced)
        registrations.register(holder)
        val changeCountBefore = proxy.changeCount

        registrations.unregister(replaced.serverId)

        assertEquals(changeCountBefore, proxy.changeCount)
    }

    @Test
    fun `hands the name to a replaced server when the holder is unregistered`() {
        val replaced = registeredServer("replaced-id", "lobby-1")
        val holder = registeredServer("holder-id", "lobby-1")
        registrations.register(replaced)
        registrations.register(holder)

        registrations.unregister(holder.serverId)

        assertEquals(replaced, proxy.serverNamed(replaced.proxyName))
        assertEquals(Optional.of(replaced.proxyName), registrations.proxyNameOf(replaced.serverId))
        assertEquals(Optional.empty(), registrations.proxyNameOf(holder.serverId))
    }

    @Test
    fun `doesn't map the successor before the proxy routes the name to it`() {
        val replaced = registeredServer("replaced-id", "lobby-1")
        val holder = registeredServer("holder-id", "lobby-1")
        registrations.register(replaced)
        registrations.register(holder)
        val answersBeforeSwitch = recordAnswersBeforeSwitch()

        registrations.unregister(holder.serverId)

        assertEquals(listOf(Optional.empty()), answersBeforeSwitch)
    }

    @Test
    fun `doesn't map a replaced server that registers again before the proxy routes the name to it`() {
        val replaced = registeredServer("replaced-id", "lobby-1")
        val holder = registeredServer("holder-id", "lobby-1")
        registrations.register(replaced)
        registrations.register(holder)
        val answersBeforeSwitch = recordAnswersBeforeSwitch()

        registrations.register(replaced)

        assertEquals(listOf(Optional.empty()), answersBeforeSwitch)
    }

    @Test
    fun `removes the name from the proxy when its last server is unregistered`() {
        val lobby = registeredServer("lobby-id", "lobby-1")
        registrations.register(lobby)

        registrations.unregister(lobby.serverId)

        assertNull(proxy.serverNamed(lobby.proxyName))
        assertEquals(Optional.empty(), registrations.proxyNameOf(lobby.serverId))
    }

    @Test
    fun `moves a server to its new proxy name`() {
        val lobby = registeredServer("lobby-id", "lobby-1")
        val renamedLobby = lobby.copy(proxyName = "hub-1")
        registrations.register(lobby)

        registrations.register(renamedLobby)

        assertNull(proxy.serverNamed(lobby.proxyName))
        assertEquals(renamedLobby, proxy.serverNamed(renamedLobby.proxyName))
        assertEquals(Optional.of(renamedLobby.proxyName), registrations.proxyNameOf(lobby.serverId))
    }

    // Records what the lookup answers for each server just before the proxy switches a name over to it.
    private fun recordAnswersBeforeSwitch(): List<Optional<String>> {
        val answers = CopyOnWriteArrayList<Optional<String>>()
        proxy.beforeRegister { server -> answers.add(registrations.proxyNameOf(server.serverId)) }
        return answers
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
