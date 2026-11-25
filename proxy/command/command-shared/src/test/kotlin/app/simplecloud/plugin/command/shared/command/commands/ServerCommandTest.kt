package app.simplecloud.plugin.command.shared.command.commands

import app.simplecloud.api.testing.TestCloud
import app.simplecloud.plugin.command.shared.command.TestCommands
import kotlin.test.*

class ServerCommandTest {

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
    fun `command is sent to a group server`() {
        cloud.createGroup("lobby").startServer()

        val messages = commands.execute("cloud server command lobby-1 say hello world")

        assertContains(messages, "Sent say hello world to server lobby-1.")
    }

    @Test
    fun `command is sent to a persistent server without the slash`() {
        cloud.createPersistentServer("survival").start()

        val messages = commands.execute("cloud server command survival /stop")

        assertContains(messages, "Sent stop to server survival.")
    }

    @Test
    fun `command to an unknown server is rejected`() {
        cloud.createGroup("lobby").startServer()

        val messages = commands.execute("cloud server command lobby-9 stop")

        assertContains(messages, "Server lobby-9 was not found.")
    }

    @Test
    fun `info shows the runtime details`() {
        cloud.createGroup("lobby").startServer()

        val messages = commands.execute("cloud server info lobby-1")

        assertContains(messages, "Server lobby-1")
        assertContains(messages, "Address:")
        assertContains(messages, "Uptime:")
        assertContains(messages, "Blueprint:")
    }

    @Test
    fun `persistent server is stopped by name`() {
        cloud.createPersistentServer("survival").start()

        val messages = commands.execute("cloud server stop survival")

        assertContains(messages, "Sent the stop request for server survival.")
    }

    private fun assertContains(messages: List<String>, expected: String) {
        assertTrue(messages.any { expected in it }, "Expected \"$expected\" in $messages")
    }
}
