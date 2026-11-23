package app.simplecloud.plugin.core.shared.config.migration

import app.simplecloud.plugin.core.shared.config.VersionedConfig
import app.simplecloud.plugin.core.shared.config.YamlFileConfigurator
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConfigMigrationsTest {

    private val directory = Files.createTempDirectory("config-migrations").toFile()
    private val file = File(directory, "config.yml")

    @Test
    fun `migrates an unversioned file through all versions`() {
        val original = """
            name: Lobby
            colors:
              primary: '#ffffff'
            legacy: true
            servers:
              lobby:
                ip: 127.0.0.1
        """.trimIndent()
        file.writeText(original)

        val config = load(TestMigrations)

        assertEquals(3, config.version)
        assertEquals("Lobby", config.title)
        assertEquals(mapOf("primary" to "#ffffff"), config.palette)
        assertEquals(mapOf("lobby" to TestServer("127.0.0.1")), config.servers)
        assertFalse(file.readText().contains("legacy"))
        assertEquals(original, File(directory, "config.yml.unversioned.bak").readText())
    }

    @Test
    fun `only applies the migrations newer than the file version`() {
        file.writeText("version: 2\nname: Old\ntitle: New\nlegacy: true\n")

        val config = load(TestMigrations)

        assertEquals(3, config.version)
        assertEquals("New", config.title)
        assertFalse(file.readText().contains("legacy"))
        assertTrue(File(directory, "config.yml.v2.bak").exists())
    }

    @Test
    fun `leaves an up to date file untouched`() {
        val original = "version: 3\ntitle: Lobby\n"
        file.writeText(original)

        val config = load(TestMigrations)

        assertEquals("Lobby", config.title)
        assertEquals(original, file.readText())
        assertEquals(listOf("config.yml"), directory.list().orEmpty().toList())
    }

    @Test
    fun `rejects a file from a newer version`() {
        file.writeText("version: 4\ntitle: Lobby\n")

        assertFailsWith<IllegalArgumentException> { load(TestMigrations) }
    }

    @Test
    fun `sets the version even without migrations`() {
        file.writeText("title: Lobby\n")

        val config = load(EmptyMigrations)

        assertEquals(2, config.version)
        assertEquals("Lobby", config.title)
    }

    @Test
    fun `requires a positive version`() {
        assertFailsWith<IllegalArgumentException> { InvalidMigrations() }
    }

    private fun load(migrations: ConfigMigrations): TestConfig {
        val configurator = YamlFileConfigurator(TestConfig::class.java, migrations)
        return checkNotNull(configurator.load(file))
    }

    @ConfigSerializable
    data class TestConfig(
        override val version: Int = 0,
        val title: String = "",
        val palette: Map<String, String> = emptyMap(),
        val servers: Map<String, TestServer> = emptyMap(),
    ) : VersionedConfig

    @ConfigSerializable
    data class TestServer(
        val address: String = "",
    )

    private object TestMigrations : ConfigMigrations(3) {
        override val migrations = listOf(
            rename(2, "name", "title"),
            move(2, "colors.primary", "palette.primary"),
            remove(3, "legacy"),
            rename(3, "servers.*.ip", "address"),
        )
    }

    private object EmptyMigrations : ConfigMigrations(2) {
        override val migrations = emptyList<ConfigMigration>()
    }

    private class InvalidMigrations : ConfigMigrations(0) {
        override val migrations = emptyList<ConfigMigration>()
    }
}
