package app.simplecloud.plugin.command.shared.command.commands

import app.simplecloud.api.group.CreateGroupRequest
import app.simplecloud.api.group.ScalingConfig
import app.simplecloud.api.testing.TestCloud
import app.simplecloud.plugin.command.shared.command.TestCommands
import kotlin.test.*

class GroupCommandTest {

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
    fun `stop without ids stops all servers of the group`() {
        val lobby = cloud.createGroup("lobby")
        lobby.startServer()
        lobby.startServer()

        val messages = commands.execute("cloud group stop lobby")

        assertContains(messages, "Sent the stop request for 2 servers of group lobby.")
        assertTrue(cloud.servers().none { it.isRunning })
    }

    @Test
    fun `stop with ids only stops the given servers`() {
        val lobby = cloud.createGroup("lobby")
        repeat(3) { lobby.startServer() }

        val messages = commands.execute("cloud group stop lobby 1,3")

        assertContains(messages, "Sent the stop request for servers 1, 3 of group lobby.")
        assertEquals(listOf("lobby-2"), cloud.servers().filter { it.isRunning }.map { it.name })
    }

    @Test
    fun `stop with unknown ids names the missing servers`() {
        cloud.createGroup("lobby").startServer()

        val messages = commands.execute("cloud group stop lobby 1 7")

        assertContains(messages, "Servers 7 of group lobby were not found.")
        assertTrue(cloud.servers().all { it.isRunning })
    }

    @Test
    fun `stop with invalid ids is rejected`() {
        cloud.createGroup("lobby").startServer()

        val messages = commands.execute("cloud group stop lobby one")

        assertContains(messages, "one are not valid server ids.")
    }

    @Test
    fun `start does not go over the max servers of the group`() {
        cloud.createGroup(limitedGroup("lobby", 2)).startServer()

        val messages = commands.execute("cloud group start lobby 3")

        assertContains(messages, "Group lobby can only start 1 more servers (1/2 running or queued).")
        assertEquals(1, cloud.servers().size)
    }

    @Test
    fun `info shows the scaling and the rollout`() {
        cloud.createGroup(limitedGroup("lobby", 2))

        val messages = commands.execute("cloud group info lobby")

        assertContains(messages, "Scaling: 0-2 servers")
        assertContains(messages, "Rollout: Inactive")
    }

    @Test
    fun `rollout replaces the running servers`() {
        val server = cloud.createGroup("lobby").startServer()

        val messages = commands.execute("cloud group rollout lobby")

        assertContains(messages, "Started the rollout of group lobby.")
        assertTrue(!server.isRunning)
        assertEquals(1, cloud.servers().count { it.isRunning })
    }

    @Test
    fun `finished rollouts are shown as inactive`() {
        cloud.createGroup("lobby").startServer()
        commands.execute("cloud group rollout lobby")

        assertContains(commands.execute("cloud group rollout lobby status"), "Group lobby has no active rollout.")
        assertContains(commands.execute("cloud group rollout lobby cancel"), "Group lobby has no active rollout to cancel.")
    }

    @Test
    fun `rollout needs running servers`() {
        cloud.createGroup("lobby")

        val messages = commands.execute("cloud group rollout lobby")

        assertContains(messages, "Group lobby has no running servers to roll out.")
    }

    private fun limitedGroup(name: String, maxServers: Int): CreateGroupRequest =
        CreateGroupRequest.builder()
            .name(name)
            .scaling(ScalingConfig.builder().maxServers(maxServers).build())
            .build()

    private fun assertContains(messages: List<String>, expected: String) {
        assertTrue(messages.any { expected in it }, "Expected \"$expected\" in $messages")
    }
}
