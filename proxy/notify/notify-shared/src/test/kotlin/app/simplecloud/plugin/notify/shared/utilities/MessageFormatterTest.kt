package app.simplecloud.plugin.notify.shared.utilities

import app.simplecloud.api.testing.TestCloud
import app.simplecloud.api.testing.event.TestEvents
import app.simplecloud.plugin.notify.shared.config.Hover
import app.simplecloud.plugin.notify.shared.config.MessageConfig
import app.simplecloud.plugin.notify.shared.config.Notifications
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentIteratorType
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageFormatterTest {

    private val cloud = TestCloud.create()
    private val config = MessageConfig(
        notifications = Notifications(
            hover = Hover(stopped = "<state>: <exit_code> <reason>"),
        ),
    )

    @AfterTest
    fun close() {
        cloud.close()
    }

    @Test
    fun `group servers are named after the group and their numerical id`() {
        val lobby = cloud.createGroup("lobby").startServer()

        val message = MessageFormatter.server(config, lobby, "<kind> <server> <group><id>")

        assertEquals("Server lobby-1 lobby 1", plain(message))
    }

    @Test
    fun `persistent servers are named without a numerical id`() {
        val survival = cloud.createPersistentServer("survival").start()

        val message = MessageFormatter.server(config, survival, "<kind> <server><id>")

        assertEquals("Persistent server survival", plain(message))
    }

    @Test
    fun `stopped servers show the exit code and reason`() {
        val lobby = cloud.createGroup("lobby").startServer()
        val event = TestEvents.serverCrashed(lobby, 137, "out of memory")

        val message = MessageFormatter.stoppedServer(config, event, "<state> <exit_code> <reason>")

        assertEquals("Crashed 137 out of memory", plain(message))
    }

    @Test
    fun `missing exit code and reason are shown as unknown`() {
        val lobby = cloud.createGroup("lobby").startServer()
        val event = TestEvents.serverStopped(lobby)

        val message = MessageFormatter.stoppedServer(config, event, "<state> <exit_code> <reason>")

        assertEquals("Stopped N/A N/A", plain(message))
    }

    @Test
    fun `hover placeholders are replaced with the configured hover text`() {
        val lobby = cloud.createGroup("lobby").startServer()
        val event = TestEvents.serverCrashed(lobby, 1, "boom")

        val message = MessageFormatter.stoppedServer(
            config,
            event,
            "<hover:show_text:'<notifications.hover.stopped>'><server></hover>",
        )

        assertEquals("lobby-1", plain(message))
        assertEquals("Crashed: 1 boom", plain(hoverText(message)))
    }

    private fun hoverText(message: Component): Component = message.iterable(ComponentIteratorType.DEPTH_FIRST)
        .firstNotNullOfOrNull { component -> component.hoverEvent()?.value() as? Component }
        ?: Component.empty()

    private fun plain(message: Component): String = PlainTextComponentSerializer.plainText().serialize(message)
}
