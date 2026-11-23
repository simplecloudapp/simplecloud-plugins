package app.simplecloud.plugin.connection.shared.registration

import app.simplecloud.api.blueprint.CreateBlueprintRequest
import app.simplecloud.api.group.CreateGroupRequest
import app.simplecloud.api.group.GroupServerType
import app.simplecloud.api.group.SourceConfig
import app.simplecloud.api.group.SourceType
import app.simplecloud.api.server.ServerApi
import app.simplecloud.api.server.ServerState
import app.simplecloud.api.testing.TestCloud
import app.simplecloud.plugin.connection.shared.config.ConnectionConfig
import app.simplecloud.plugin.connection.shared.config.RegistrationConfig
import app.simplecloud.plugin.connection.shared.config.RegistrationServer
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class ServerRegistererTest {

    private val cloud = TestCloud.create()
    private val registry = TestServerRegistry()
    private val registrations = ServerRegistrations(registry)
    private val statusCache = ServerStatusCache(registrations)
    private val registerers = mutableListOf<ServerRegisterer>()

    @AfterTest
    fun close() {
        registerers.forEach { registerer -> registerer.stop() }
        cloud.close()
    }

    @Test
    fun `sync registers running servers under their proxy name`() {
        val lobby = cloud.createGroup("lobby").startServer()

        sync()

        val registered = registry.findServer("lobby-1")
        assertEquals(lobby.serverId, registered?.serverId)
        assertEquals(lobby.ip, registered?.ip)
        assertEquals(lobby.port, registered?.port)
    }

    @Test
    fun `sync registers persistent servers with the persistent name pattern`() {
        val survival = cloud.createPersistentServer("survival").start()

        sync()

        assertEquals(survival.serverId, registry.findServer("survival")?.serverId)
    }

    @Test
    fun `sync ignores proxy servers`() {
        cloud.createGroup(CreateGroupRequest.builder().name("proxy").type(GroupServerType.PROXY).build()).startServer()

        sync()

        assertNull(registry.findServer("proxy-1"))
    }

    @Test
    fun `sync ignores servers with a standalone blueprint`() {
        val blueprint = cloud.createBlueprint(
            CreateBlueprintRequest.builder()
                .name("standalone")
                .configurator("standalone")
                .serverSoftware("paper")
                .minecraftVersion("1.21.4")
                .softwareVersion("1.21.4")
                .build(),
        )
        val source = SourceConfig.builder().type(SourceType.BLUEPRINT).blueprint(blueprint.blueprintId).build()
        cloud.createGroup(CreateGroupRequest.builder().name("standalone").source(source).build()).startServer()
        cloud.createGroup("lobby").startServer()

        sync()

        assertNull(registry.findServer("standalone-1"))
        assertEquals("lobby", registry.findServer("lobby-1")?.serverBaseName)
    }

    @Test
    fun `sync ignores configured groups`() {
        cloud.createGroup("lobby").startServer()

        sync(RegistrationConfig(ignoreServerGroupsAndPersistentServers = listOf("Lobby")))

        assertNull(registry.findServer("lobby-1"))
    }

    @Test
    fun `sync skips servers whose name is used by an additional server`() {
        cloud.createGroup("lobby").startServer()

        sync(RegistrationConfig(additionalServers = listOf(RegistrationServer("lobby-1", "10.0.0.1", 25565))))

        assertNull(registry.findServer("lobby-1"))
    }

    @Test
    fun `sync stores the network wide player counts`() {
        cloud.createGroup("lobby").startServer().setPlayerCount(7).setMaxPlayers(20)

        sync()

        assertEquals(ServerStatus(false, 7, 20), statusCache.findAllByProxyName()["lobby-1"])
    }

    @Test
    fun `stopping servers stay registered but are marked as stopping`() {
        val registerer = registerer()
        val lobby = cloud.createGroup("lobby").startServer().setPlayerCount(3)
        runBlocking { registerer.sync() }

        lobby.setState(ServerState.STOPPING)
        runBlocking { registerer.sync() }

        assertEquals(lobby.serverId, registry.findServer("lobby-1")?.serverId)
        assertEquals(true, statusCache.findAllByProxyName()["lobby-1"]?.stopping)
    }

    @Test
    fun `sync unregisters servers that are no longer running`() {
        val registerer = registerer()
        val lobby = cloud.createGroup("lobby").startServer()
        runBlocking { registerer.sync() }

        lobby.stop()
        runBlocking { registerer.sync() }

        assertNull(registry.findServer("lobby-1"))
        assertNull(registrations.find(lobby.serverId))
    }

    @Test
    fun `servers that become available are registered without waiting for the next sync`() {
        val registerer = registerer(RegistrationConfig(syncInterval = 3600))
        cloud.network().failAlways(ServerApi::class.java, "getAllServers")
        cloud.controller().startState(ServerState.STARTING)
        val lobby = cloud.createGroup("lobby").startServer()
        registerer.start()

        lobby.setState(ServerState.AVAILABLE)

        runBlocking {
            withTimeout(5.seconds) {
                while (registry.findServer("lobby-1")?.serverId != lobby.serverId) {
                    delay(10)
                }
            }
        }
    }

    @Test
    fun `stopped servers are unregistered without waiting for the next sync`() {
        val registerer = registerer(RegistrationConfig(syncInterval = 3600))
        val lobby = cloud.createGroup("lobby").startServer()
        runBlocking { registerer.sync() }
        registerer.start()

        lobby.stop()

        runBlocking {
            withTimeout(5.seconds) {
                while (registry.findServer("lobby-1") != null) {
                    delay(10)
                }
            }
        }
    }

    private fun sync(registration: RegistrationConfig = RegistrationConfig()) {
        val registerer = registerer(registration)
        runBlocking { registerer.sync() }
    }

    private fun registerer(registration: RegistrationConfig = RegistrationConfig()): ServerRegisterer {
        val file = Files.createTempFile("server-connection", ".yml").toFile()
        file.deleteOnExit()
        val config = ConfigurationFactory(file, ConnectionConfig::class.java)
        config.save(ConnectionConfig(registration = registration))

        val registerer = ServerRegisterer(cloud.api(), registrations, statusCache, config)
        registerers.add(registerer)
        return registerer
    }
}
