package app.simplecloud.plugin.connection.shared.connection

import app.simplecloud.plugin.connection.shared.config.AddressConfig
import app.simplecloud.plugin.connection.shared.config.CommandEntry
import app.simplecloud.plugin.connection.shared.config.CommandMessages
import app.simplecloud.plugin.connection.shared.config.ConnectionConfig
import app.simplecloud.plugin.connection.shared.config.ConnectionEntry
import app.simplecloud.plugin.connection.shared.config.ConnectionRule
import app.simplecloud.plugin.connection.shared.config.FallbackConfig
import app.simplecloud.plugin.connection.shared.config.FallbackTargetConnection
import app.simplecloud.plugin.connection.shared.config.MessageConfig
import app.simplecloud.plugin.connection.shared.config.NetworkJoinTargetsConfig
import app.simplecloud.plugin.connection.shared.config.RuleType
import app.simplecloud.plugin.connection.shared.config.SubdomainRoute
import app.simplecloud.plugin.connection.shared.config.TargetConnection
import app.simplecloud.plugin.connection.shared.platform.ConnectionServer
import app.simplecloud.plugin.connection.shared.registration.RegisteredServer
import app.simplecloud.plugin.connection.shared.registration.ServerRegistrations
import app.simplecloud.plugin.connection.shared.registration.ServerStatus
import app.simplecloud.plugin.connection.shared.registration.ServerStatusCache
import app.simplecloud.plugin.connection.shared.registration.TestServerRegistry
import app.simplecloud.core.shared.matcher.OperationType
import app.simplecloud.core.shared.matcher.ServerMatcherConfiguration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConnectionResolverTest {

    private val registrations = ServerRegistrations(TestServerRegistry())
    private val statusCache = ServerStatusCache(registrations)

    private val lobby = ConnectionEntry(
        name = "lobby",
        serverNameMatcher = ServerMatcherConfiguration(OperationType.STARTS_WITH, "lobby"),
    )
    private val vipLobby = ConnectionEntry(
        name = "vip-lobby",
        serverNameMatcher = ServerMatcherConfiguration(OperationType.STARTS_WITH, "viplobby"),
        rules = listOf(
            ConnectionRule(type = RuleType.PERMISSION, name = "lobby.vip", bypassPermission = "lobby.admin"),
        ),
    )
    private val bedwars = ConnectionEntry(
        name = "bedwars",
        serverNameMatcher = ServerMatcherConfiguration(OperationType.STARTS_WITH, "bedwars"),
    )

    private val config = ConnectionConfig(
        connections = listOf(lobby, vipLobby, bedwars),
        networkJoinTargets = NetworkJoinTargetsConfig(
            targetConnections = listOf(TargetConnection("lobby", 0), TargetConnection("vip-lobby", 10)),
        ),
        fallback = FallbackConfig(targetConnections = listOf(FallbackTargetConnection("lobby", 0))),
    )
    private val messages = MessageConfig()

    private val lobbyCommand = CommandEntry(
        name = "lobby",
        messages = CommandMessages(alreadyConnected = "Already on <server>", noTargetConnectionFound = "No lobby"),
        targetConnections = listOf(FallbackTargetConnection("lobby", 0)),
    )

    @Test
    fun `join picks the server with the fewest players`() {
        val resolver = resolver(listOf(server("lobby-1", 5), server("lobby-2", 2), server("bedwars-1", 0)))

        assertEquals("lobby-2", resolver.findJoinTarget(config, TestConnectionPlayer()))
    }

    @Test
    fun `join prefers the target connection with the highest priority`() {
        val resolver = resolver(listOf(server("lobby-1", 0), server("viplobby-1", 20)))
        val player = TestConnectionPlayer(permissions = setOf("lobby.vip"))

        assertEquals("viplobby-1", resolver.findJoinTarget(config, player))
    }

    @Test
    fun `join skips connections whose rules do not pass`() {
        val resolver = resolver(listOf(server("lobby-1", 0), server("viplobby-1", 0)))

        assertEquals("lobby-1", resolver.findJoinTarget(config, TestConnectionPlayer()))
    }

    @Test
    fun `rule bypass permission lets players through`() {
        val resolver = resolver(listOf(server("lobby-1", 0), server("viplobby-1", 0)))
        val player = TestConnectionPlayer(permissions = setOf("lobby.admin"))

        assertEquals("viplobby-1", resolver.findJoinTarget(config, player))
    }

    @Test
    fun `join ignores stopping servers`() {
        register("lobby-1", stopping = true)
        val resolver = resolver(listOf(server("lobby-1", 0), server("lobby-2", 3)))

        assertEquals("lobby-2", resolver.findJoinTarget(config, TestConnectionPlayer()))
    }

    @Test
    fun `join uses the network wide player count when it is higher`() {
        register("lobby-1", playerCount = 10)
        val resolver = resolver(listOf(server("lobby-1", 1), server("lobby-2", 5)))

        assertEquals("lobby-2", resolver.findJoinTarget(config, TestConnectionPlayer()))
    }

    @Test
    fun `join prefers servers with free slots over full ones`() {
        register("lobby-1", playerCount = 2, maxPlayers = 2)
        register("lobby-2", playerCount = 8, maxPlayers = 10)
        val resolver = resolver(listOf(server("lobby-1", 2), server("lobby-2", 8)))

        assertEquals("lobby-2", resolver.findJoinTarget(config, TestConnectionPlayer()))
    }

    @Test
    fun `join picks the least populated full server when all servers are full`() {
        register("lobby-1", playerCount = 10, maxPlayers = 10)
        register("lobby-2", playerCount = 5, maxPlayers = 5)
        val resolver = resolver(listOf(server("lobby-1", 10), server("lobby-2", 5)))

        assertEquals("lobby-2", resolver.findJoinTarget(config, TestConnectionPlayer()))
    }

    @Test
    fun `servers without max players count as free`() {
        register("lobby-1", playerCount = 50, maxPlayers = 0)
        register("lobby-2", playerCount = 5, maxPlayers = 5)
        val resolver = resolver(listOf(server("lobby-1", 50), server("lobby-2", 5)))

        assertEquals("lobby-1", resolver.findJoinTarget(config, TestConnectionPlayer()))
    }

    @Test
    fun `join routes by subdomain ignoring case`() {
        val routedConfig = config.copy(address = AddressConfig(listOf(SubdomainRoute("bw.example.com", "bedwars"))))
        val resolver = resolver(listOf(server("lobby-1", 0), server("bedwars-1", 10)))
        val routedPlayer = TestConnectionPlayer(virtualHost = "BW.Example.com")
        val otherPlayer = TestConnectionPlayer(virtualHost = "play.example.com")

        assertEquals("bedwars-1", resolver.findJoinTarget(routedConfig, routedPlayer))
        assertEquals("lobby-1", resolver.findJoinTarget(routedConfig, otherPlayer))
    }

    @Test
    fun `join returns no server when network join targets are disabled`() {
        val disabledConfig = config.copy(networkJoinTargets = config.networkJoinTargets.copy(enabled = false))
        val resolver = resolver(listOf(server("lobby-1", 0)))

        assertNull(resolver.findJoinTarget(disabledConfig, TestConnectionPlayer()))
    }

    @Test
    fun `join skips excluded servers ignoring case`() {
        val resolver = resolver(listOf(server("lobby-1", 0), server("lobby-2", 5)))

        assertEquals("lobby-2", resolver.findJoinTarget(config, TestConnectionPlayer(), setOf("LOBBY-1")))
        assertNull(resolver.findJoinTarget(config, TestConnectionPlayer(), setOf("lobby-1", "lobby-2")))
    }

    @Test
    fun `fallback never picks the kicked server`() {
        val resolver = resolver(listOf(server("lobby-1", 0), server("lobby-2", 5)))

        assertEquals("lobby-2", resolver.findFallbackTarget(config, TestConnectionPlayer(), "lobby-1"))
    }

    @Test
    fun `fallback skips servers that were already tried`() {
        val resolver = resolver(listOf(server("lobby-1", 0), server("lobby-2", 1), server("lobby-3", 2)))
        val player = TestConnectionPlayer()

        assertEquals("lobby-3", resolver.findFallbackTarget(config, player, "lobby-1", setOf("lobby-1", "lobby-2")))
        assertNull(resolver.findFallbackTarget(config, player, "lobby-3", setOf("lobby-1", "lobby-2", "lobby-3")))
    }

    @Test
    fun `fallback from filter matches servers that are no longer registered`() {
        val fromConfig = config.copy(
            fallback = FallbackConfig(
                targetConnections = listOf(FallbackTargetConnection("lobby", 0, listOf("bedwars"))),
            ),
        )
        val resolver = resolver(listOf(server("lobby-1", 0)))

        assertEquals("lobby-1", resolver.findFallbackTarget(fromConfig, TestConnectionPlayer(), "bedwars-1"))
        assertNull(resolver.findFallbackTarget(fromConfig, TestConnectionPlayer(), "skywars-1"))
    }

    @Test
    fun `command connects the player to the least populated target server`() {
        val player = TestConnectionPlayer(serverName = "bedwars-1")
        val resolver = resolver(listOf(server("lobby-1", 4), server("lobby-2", 1), server("bedwars-1", 3)))

        resolver.executeCommand(config, messages, player, lobbyCommand)

        assertEquals(listOf("lobby-2"), player.connectedServers)
        assertTrue(player.messages.isEmpty())
    }

    @Test
    fun `command tells the player when they are already connected`() {
        val player = TestConnectionPlayer(serverName = "lobby-1")
        val resolver = resolver(listOf(server("lobby-1", 4), server("lobby-2", 1)))

        resolver.executeCommand(config, messages, player, lobbyCommand)

        assertTrue(player.connectedServers.isEmpty())
        assertEquals(listOf("Already on lobby-1"), plainMessages(player))
    }

    @Test
    fun `command moves the player off a stopping server`() {
        register("lobby-1", stopping = true)
        val player = TestConnectionPlayer(serverName = "lobby-1")
        val resolver = resolver(listOf(server("lobby-1", 0), server("lobby-2", 5)))

        resolver.executeCommand(config, messages, player, lobbyCommand)

        assertEquals(listOf("lobby-2"), player.connectedServers)
    }

    @Test
    fun `command tells the player when no server is available`() {
        val player = TestConnectionPlayer(serverName = "bedwars-1")
        val resolver = resolver(listOf(server("bedwars-1", 3)))

        resolver.executeCommand(config, messages, player, lobbyCommand)

        assertTrue(player.connectedServers.isEmpty())
        assertEquals(listOf("No lobby"), plainMessages(player))
    }

    @Test
    fun `find connection ignores case`() {
        val resolver = resolver(emptyList())

        assertEquals(lobby, resolver.findConnection("LOBBY", config.connections))
        assertNull(resolver.findConnection("skywars", config.connections))
    }

    @Test
    fun `permission rule with false only lets players without the permission through`() {
        val guestLobby = ConnectionEntry(
            name = "guest-lobby",
            serverNameMatcher = ServerMatcherConfiguration(OperationType.STARTS_WITH, "guestlobby"),
            rules = listOf(ConnectionRule(type = RuleType.PERMISSION, name = "lobby.member", value = "false")),
        )
        val guestConfig = config.copy(
            connections = listOf(lobby, guestLobby),
            networkJoinTargets = NetworkJoinTargetsConfig(
                targetConnections = listOf(TargetConnection("lobby", 0), TargetConnection("guest-lobby", 10)),
            ),
        )
        val resolver = resolver(listOf(server("lobby-1", 0), server("guestlobby-1", 0)))

        assertEquals("guestlobby-1", resolver.findJoinTarget(guestConfig, TestConnectionPlayer()))
        assertEquals(
            "lobby-1",
            resolver.findJoinTarget(guestConfig, TestConnectionPlayer(permissions = setOf("lobby.member"))),
        )
    }

    @Test
    fun `join falls back to the network join targets when the routed connection has no server`() {
        val routedConfig = config.copy(address = AddressConfig(listOf(SubdomainRoute("bw.example.com", "bedwars"))))
        val resolver = resolver(listOf(server("lobby-1", 0)))
        val player = TestConnectionPlayer(virtualHost = "bw.example.com")

        assertEquals("lobby-1", resolver.findJoinTarget(routedConfig, player))
    }

    @Test
    fun `fallback prefers the target connection with the highest priority`() {
        val priorityConfig = config.copy(
            fallback = FallbackConfig(
                targetConnections = listOf(
                    FallbackTargetConnection("lobby", 0),
                    FallbackTargetConnection("bedwars", 5),
                ),
            ),
        )
        val resolver = resolver(listOf(server("lobby-1", 0), server("bedwars-1", 10)))

        assertEquals("bedwars-1", resolver.findFallbackTarget(priorityConfig, TestConnectionPlayer(), "skywars-1"))
    }

    @Test
    fun `fallback returns no server when no connection passes`() {
        val resolver = resolver(listOf(server("bedwars-1", 0)))

        assertNull(resolver.findFallbackTarget(config, TestConnectionPlayer(), "lobby-1"))
    }

    @Test
    fun `command skips targets whose from filter does not match the current server`() {
        val command = lobbyCommand.copy(
            targetConnections = listOf(
                FallbackTargetConnection("bedwars", 10, listOf("lobby")),
                FallbackTargetConnection("lobby", 0),
            ),
        )
        val player = TestConnectionPlayer(serverName = "skywars-1")
        val resolver = resolver(listOf(server("lobby-1", 0), server("bedwars-1", 0)))

        resolver.executeCommand(config, messages, player, command)

        assertEquals(listOf("lobby-1"), player.connectedServers)
    }

    private fun resolver(servers: List<ConnectionServer>): ConnectionResolver =
        ConnectionResolver(TestConnectionPlatform(servers), statusCache)

    private fun server(name: String, playerCount: Int): ConnectionServer = ConnectionServer(name, playerCount)

    private fun register(name: String, stopping: Boolean = false, playerCount: Int = 0, maxPlayers: Int = 0) {
        registrations.register(
            RegisteredServer(
                serverId = name,
                numericalId = 1,
                ip = "127.0.0.1",
                port = 25565,
                serverBaseName = name,
                proxyName = name,
                properties = emptyMap(),
                persistent = false,
            ),
        )
        statusCache.update(name, ServerStatus(stopping, playerCount, maxPlayers))
    }

    private fun plainMessages(player: TestConnectionPlayer): List<String> =
        player.messages.map { message -> PlainTextComponentSerializer.plainText().serialize(message) }
}
