package app.simplecloud.plugin.command.shared.command.commands

import app.simplecloud.api.server.Server
import app.simplecloud.api.testing.TestCloud
import app.simplecloud.api.testing.player.TestPlayer
import app.simplecloud.plugin.command.shared.command.TestCommands
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import kotlin.test.*

class PlayerCommandTest {

    private val cloud = TestCloud.create()
    private val commands = TestCommands(cloud)

    @BeforeTest
    fun register() {
        commands.register()
    }

    @AfterTest
    fun close() {
        commands.shutdown()
        cloud.close()
    }

    @Test
    fun `send moves a player to a specific server`() {
        val lobby = cloud.createGroup("lobby")
        val player = onlinePlayer(lobby.startServer())
        lobby.startServer()

        val messages = commands.execute("cloud player send Steve server lobby-2")

        assertContains(messages, "Sent")
        assertEquals("lobby-2", cloud.player(player.uniqueId).orElseThrow().connectedServerName)
    }

    @Test
    fun `send to a group accepts a selection mode`() {
        val player = onlinePlayer(cloud.createGroup("lobby").startServer())
        cloud.createGroup("game").startServer()

        val messages = commands.execute("cloud player send Steve group game min_players")

        assertContains(messages, "Sent")
        assertEquals("game-1", cloud.player(player.uniqueId).orElseThrow().connectedServerName)
    }

    @Test
    fun `send rejects an unknown selection mode`() {
        onlinePlayer(cloud.createGroup("lobby").startServer())

        val messages = commands.execute("cloud player send Steve group lobby nope")

        assertContains(messages, "nope is not an option")
    }

    @Test
    fun `send rejects a selection mode for a server target`() {
        val lobby = cloud.createGroup("lobby")
        onlinePlayer(lobby.startServer())
        lobby.startServer()

        val messages = commands.execute("cloud player send Steve server lobby-2 first")

        assertContains(messages, "A selection mode only works with a group as the target.")
    }

    @Test
    fun `kick disconnects the player`() {
        val player = onlinePlayer(cloud.createGroup("lobby").startServer())

        val messages = commands.execute("cloud player kick Steve <red>Maintenance")

        assertContains(messages, "Kicked")
        assertFalse(cloud.player(player.uniqueId).orElseThrow().isOnline)
    }

    @Test
    fun `kick of an offline player is rejected`() {
        cloud.createPlayer("Steve")

        val messages = commands.execute("cloud player kick Steve")

        assertContains(messages, "Player Steve is not online.")
    }

    @Test
    fun `message is parsed with MiniMessage`() {
        val player = onlinePlayer(cloud.createGroup("lobby").startServer())

        commands.execute("cloud player message Steve <red>Hello</red> there")

        val received = player.messages().map { PlainTextComponentSerializer.plainText().serialize(it) }
        assertEquals(listOf("Hello there"), received)
    }

    @Test
    fun `info shows the unique id`() {
        val player = onlinePlayer(cloud.createGroup("lobby").startServer())

        val messages = commands.execute("cloud player info Steve")

        assertContains(messages, "UUID: ${player.uniqueId}")
    }

    private fun onlinePlayer(server: Server): TestPlayer =
        cloud.createPlayer("Steve").login().join(server)

    private fun assertContains(messages: List<String>, expected: String) {
        assertTrue(messages.any { expected in it }, "Expected \"$expected\" in $messages")
    }
}
