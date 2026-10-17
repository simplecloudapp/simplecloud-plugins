package app.simplecloud.plugin.api.shared.config

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.spongepowered.configurate.objectmapping.ConfigSerializable

class ConfigurationMigrationTest {

    @Test
    fun `factory migrates outdated configs and rewrites yaml`() {
        val tempDirectory = Files.createTempDirectory("config-migration-factory")
        val file = tempDirectory.resolve("config.yml").toFile()
        file.writeText(
            """
            name: legacy-value
            """.trimIndent()
        )

        val configurationFactory = ConfigurationFactory(
            file = file,
            javaClass = TestVersionedConfig::class.java,
            configMigrator = testMigrator(),
        )

        val config = configurationFactory.loadOrCreate(TestVersionedConfig())

        assertEquals(2, config.version)
        assertEquals("legacy-value", config.message)
        assertTrue(config.enabled)

        val migratedYaml = file.readText()
        assertTrue(migratedYaml.contains("version: 2"))
        assertTrue(migratedYaml.contains("message: legacy-value"))
        assertTrue(migratedYaml.contains("enabled: true"))
        assertFalse(migratedYaml.contains("name:"))
    }

    @Test
    fun `directory repository migrates loaded entries`() {
        val tempDirectory = Files.createTempDirectory("config-migration-repository")
        val file = tempDirectory.resolve("entry.yml").toFile()
        file.writeText(
            """
            version: 1
            message: repository-value
            """.trimIndent()
        )

        val repository = TestYamlDirectoryRepository(
            directory = tempDirectory,
            configMigrator = testMigrator(),
        )

        val loadedEntries = repository.load()

        assertEquals(1, loadedEntries.size)
        val config = loadedEntries.first()
        assertEquals(2, config.version)
        assertEquals("repository-value", config.message)
        assertTrue(config.enabled)

        val migratedYaml = file.readText()
        assertTrue(migratedYaml.contains("version: 2"))
        assertTrue(migratedYaml.contains("enabled: true"))
    }

    private fun testMigrator(): ConfigMigrator {
        return ConfigMigrator.builder(currentVersion = 2)
            .migrate(fromVersion = 0, toVersion = 1) { node ->
                val legacyName = node.node("name").string ?: return@migrate
                node.node("message").set(legacyName)
                node.node("name").set(null)
            }
            .migrate(fromVersion = 1, toVersion = 2) { node ->
                node.node("enabled").set(true)
            }
            .build()
    }

    @ConfigSerializable
    private class TestVersionedConfig : VersionedConfig {
        override var version: Int = 2
        var message: String = "default-message"
        var enabled: Boolean = true
    }

    private class TestYamlDirectoryRepository(
        directory: Path,
        configMigrator: ConfigMigrator,
    ) : YamlDirectoryRepository<TestVersionedConfig, String>(
        directory = directory,
        javaClass = TestVersionedConfig::class.java,
        configMigrator = configMigrator,
    ) {

        override fun save(entity: TestVersionedConfig) {
            save("${entity.message}.yml", entity)
        }

        override fun find(identifier: String): TestVersionedConfig? {
            return findAll().firstOrNull { it.message == identifier }
        }
    }
}
