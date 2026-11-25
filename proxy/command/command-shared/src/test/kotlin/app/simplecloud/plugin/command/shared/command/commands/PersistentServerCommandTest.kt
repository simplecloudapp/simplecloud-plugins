package app.simplecloud.plugin.command.shared.command.commands

import app.simplecloud.api.testing.TestCloud
import app.simplecloud.plugin.command.shared.command.TestCommands
import kotlin.test.*

class PersistentServerCommandTest {

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
    fun `list shows running and offline persistent servers`() {
        cloud.createPersistentServer("survival").start()
        cloud.createPersistentServer("creative")

        val messages = commands.execute("cloud ps list")

        assertContains(messages, "Persistent servers (2)")
        assertContains(messages, "creative (offline)")
        assertContains(messages, "available")
    }

    @Test
    fun `list without persistent servers says so`() {
        val messages = commands.execute("cloud ps list")

        assertContains(messages, "No persistent servers were found.")
    }

    @Test
    fun `info shows the state of the running server`() {
        cloud.createPersistentServer("survival").start()

        val messages = commands.execute("cloud ps info survival")

        assertContains(messages, "Persistent server survival")
        assertContains(messages, "State: Available")
    }

    @Test
    fun `info of an unknown persistent server is rejected`() {
        val messages = commands.execute("cloud ps info nope")

        assertContains(messages, "Persistent server nope was not found.")
    }

    private fun assertContains(messages: List<String>, expected: String) {
        assertTrue(messages.any { expected in it }, "Expected \"$expected\" in $messages")
    }
}
