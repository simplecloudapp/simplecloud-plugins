package app.simplecloud.plugin.connection.shared.config

import app.simplecloud.plugin.connection.shared.utilities.config.ConfigVersion
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.core.shared.matcher.OperationType
import app.simplecloud.plugin.core.shared.matcher.ServerMatcherConfiguration
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultConfigTest {

    @Test
    fun `bundled config yml loads`() {
        val factory = ConfigurationFactory(bundledFile("config.yml"), ConnectionConfig::class.java)
        factory.reload()
        val config = factory.get()

        assertEquals(ConfigVersion.VERSION, config.version)
        assertEquals(RegistrationConfig(), config.registration)
        assertEquals(
            listOf(ConnectionEntry("lobby", ServerMatcherConfiguration(OperationType.STARTS_WITH, "lobby"))),
            config.connections,
        )
        assertEquals(listOf(TargetConnection("lobby", 0)), config.networkJoinTargets.targetConnections)
        assertEquals(listOf(FallbackTargetConnection("lobby", 0)), config.fallback.targetConnections)
    }

    @Test
    fun `bundled messages yml matches the defaults`() {
        val factory = ConfigurationFactory(bundledFile("messages.yml"), MessageConfig::class.java)
        factory.reload()
        val messages = factory.get()

        assertEquals(ConfigVersion.VERSION, messages.version)
        assertEquals(KickMessages(), messages.kick)
    }

    @Test
    fun `bundled commands yml loads`() {
        val factory = ConfigurationFactory(bundledFile("commands.yml"), CommandConfig::class.java)
        factory.reload()
        val command = factory.get().commands.single()

        assertEquals("lobby", command.name)
        assertEquals(listOf("l", "hub", "quit", "leave"), command.aliases)
        assertEquals(listOf(FallbackTargetConnection("lobby", 0)), command.targetConnections)
    }

    @Test
    fun `bundled configs only reference defined connections`() {
        val configFactory = ConfigurationFactory(bundledFile("config.yml"), ConnectionConfig::class.java)
        configFactory.reload()
        val commandFactory = ConfigurationFactory(bundledFile("commands.yml"), CommandConfig::class.java)
        commandFactory.reload()
        val config = configFactory.get()

        val definedNames = config.connections.map { connection -> connection.name }
        val referencedNames = config.address.routes.map { route -> route.targetConnection } +
            config.networkJoinTargets.targetConnections.map { target -> target.name } +
            config.fallback.targetConnections.flatMap { target -> target.from + target.name } +
            commandFactory.get().commands.flatMap { command ->
                command.targetConnections.flatMap { target -> target.from + target.name }
            }

        assertTrue(referencedNames.all { name -> name in definedNames })
    }

    private fun bundledFile(name: String): File {
        val content = javaClass.classLoader.getResource(name)?.readText() ?: error("$name is missing")
        val file = Files.createTempFile("server-connection", ".yml").toFile()
        file.deleteOnExit()
        file.writeText(content)
        return file
    }
}
