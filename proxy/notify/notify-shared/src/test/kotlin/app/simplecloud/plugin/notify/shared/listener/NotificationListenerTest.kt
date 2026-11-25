package app.simplecloud.plugin.notify.shared.listener

import app.simplecloud.api.testing.TestCloud
import app.simplecloud.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.notify.shared.config.MessageConfig
import app.simplecloud.plugin.notify.shared.config.Notifications
import app.simplecloud.plugin.notify.shared.config.ServerNotifications
import app.simplecloud.plugin.notify.shared.notification.NotificationCache
import app.simplecloud.plugin.notify.shared.utilities.NotifyPermissions
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.nio.file.Files
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class NotificationListenerTest {

    private val cloud = TestCloud.create()
    private val cache = NotificationCache(cloud.api().player())
    private val listeners = mutableListOf<NotificationListener>()

    private val admin = TestNotifySender(
        null,
        setOf(NotifyPermissions.SERVER_STOPPED, NotifyPermissions.SERVER_CRASHED),
    )

    @AfterTest
    fun close() {
        listeners.forEach { listener -> listener.stop() }
        cloud.close()
    }

    @Test
    fun `crashed servers send the crashed notification`() {
        val lobby = cloud.createGroup("lobby").startServer()
        start(listOf(admin))

        lobby.crash(137, "out of memory")

        awaitMessages(admin, 1)
        assertEquals(listOf("Server lobby-1 crashed: 137 out of memory"), admin.plainMessages())
    }

    @Test
    fun `stopped servers send the stopped notification`() {
        val lobby = cloud.createGroup("lobby").startServer()
        start(listOf(admin))

        lobby.stop()

        awaitMessages(admin, 1)
        assertEquals(listOf("Server lobby-1 stopped"), admin.plainMessages())
    }

    @Test
    fun `crashed persistent servers use the persistent server name`() {
        val survival = cloud.createPersistentServer("survival").start()
        start(listOf(admin))

        survival.crash(1, null)

        awaitMessages(admin, 1)
        assertEquals(listOf("Persistent server survival crashed: 1 N/A"), admin.plainMessages())
    }

    @Test
    fun `crash notifications need the crash permission`() {
        val lobby = cloud.createGroup("lobby").startServer()
        val stopOnly = TestNotifySender(null, setOf(NotifyPermissions.SERVER_STOPPED))
        start(listOf(stopOnly, admin))

        lobby.crash()

        awaitMessages(admin, 1)
        assertTrue(stopOnly.plainMessages().isEmpty())
    }

    @Test
    fun `players who disabled crash notifications do not get them`() {
        val lobby = cloud.createGroup("lobby").startServer()
        val player = player("notify.server.crashed.enabled")
        start(listOf(player, admin))

        lobby.crash()

        awaitMessages(admin, 1)
        assertTrue(player.plainMessages().isEmpty())
    }

    @Test
    fun `players who disabled stop notifications still get crash notifications`() {
        val lobby = cloud.createGroup("lobby").startServer()
        val player = player("notify.server.stopped.enabled")
        start(listOf(player))

        lobby.crash(1, "boom")

        awaitMessages(player, 1)
        assertEquals(listOf("Server lobby-1 crashed: 1 boom"), player.plainMessages())
    }

    private fun start(senders: List<TestNotifySender>) {
        val file = Files.createTempFile("notify", ".yml").toFile()
        file.deleteOnExit()
        val messages = ConfigurationFactory(file, MessageConfig::class.java)
        messages.save(
            MessageConfig(
                notifications = Notifications(
                    server = ServerNotifications(
                        starting = null,
                        available = null,
                        stopping = null,
                        stopped = "<kind> <server> stopped",
                        crashed = "<kind> <server> crashed: <exit_code> <reason>",
                    ),
                ),
            ),
        )

        val listener = NotificationListener(cloud.api(), messages, TestNotifyPlatform(senders), cache)
        listeners.add(listener)
        listener.start()
    }

    private fun player(disabledProperty: String): TestNotifySender {
        val id = UUID.randomUUID()
        cloud.createPlayer(id, "Player").setProperty(disabledProperty, "false")
        runBlocking { cache.load(id) }
        return TestNotifySender(id, setOf(NotifyPermissions.SERVER_STOPPED, NotifyPermissions.SERVER_CRASHED))
    }

    private fun awaitMessages(sender: TestNotifySender, count: Int) {
        runBlocking {
            withTimeout(5.seconds) {
                while (sender.plainMessages().size < count) {
                    delay(10)
                }
            }
        }
    }
}
