package app.simplecloud.plugin.connection.shared.connection

import app.simplecloud.plugin.connection.shared.config.ConnectionConfig
import app.simplecloud.plugin.connection.shared.config.ConnectionEntry
import app.simplecloud.plugin.connection.shared.config.FallbackConfig
import app.simplecloud.plugin.connection.shared.config.FallbackTargetConnection
import app.simplecloud.plugin.core.shared.matcher.OperationType
import app.simplecloud.plugin.core.shared.matcher.ServerMatcherConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConnectionResolverTest {

    private val emptyLobby = FakeServer("lobby-1", 0)
    private val busyLobby = FakeServer("lobby-2", 10)
    private val fullLobby = FakeServer("lobby-3", 20)
    private val platform = FakePlatform(listOf(emptyLobby, busyLobby, fullLobby))
    private val lobbyConnection = ConnectionEntry(
        name = "lobby",
        serverNameMatcher = ServerMatcherConfiguration(OperationType.STARTS_WITH, "lobby"),
    )
    private val config = ConnectionConfig(
        connections = listOf(lobbyConnection),
        fallback = FallbackConfig(targetConnections = listOf(FallbackTargetConnection(name = "lobby"))),
    )

    @Test
    fun `picks the least populated server`() {
        val target = findFallbackTarget(excludedServerNames = emptySet())

        assertEquals(emptyLobby, target)
    }

    @Test
    fun `skips excluded servers`() {
        val excludedServerNames = setOf(emptyLobby.serverName, busyLobby.serverName)

        val target = findFallbackTarget(excludedServerNames)

        assertEquals(fullLobby, target)
    }

    @Test
    fun `skips excluded servers whose name differs only in case`() {
        val excludedServerNames = setOf("LOBBY-1")

        val target = findFallbackTarget(excludedServerNames)

        assertEquals(busyLobby, target)
    }

    @Test
    fun `finds no server when every server is excluded`() {
        val excludedServerNames = setOf(emptyLobby.serverName, busyLobby.serverName, fullLobby.serverName)

        val target = findFallbackTarget(excludedServerNames)

        assertNull(target)
    }

    private fun findFallbackTarget(excludedServerNames: Set<String>): FakeServer? =
        ConnectionResolver.findFallbackTarget(
            platform,
            FakePlayer,
            kickedServerName = null,
            excludedServerNames = excludedServerNames,
            config = config,
            stoppingServers = emptySet(),
        )
}
