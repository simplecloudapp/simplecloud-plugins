package app.simplecloud.plugin.proxy.shared.config.migration

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LegacyDirectoryMigratorTest {

    private val plugins: Path = Files.createTempDirectory("plugins")
    private val legacy: Path = plugins.resolve("proxy-essentials-velocity")
    private val directory: Path = plugins.resolve("simplecloud-proxy-essentials")

    @Test
    fun `moves the legacy directory so only one directory is left`() {
        legacy.resolve("layout").createDirectories().resolve("public.yml").writeText("legacy")

        LegacyDirectoryMigrator.migrate(directory)

        assertFalse(legacy.exists())
        assertEquals("legacy", directory.resolve("layout/public.yml").readText())
    }

    @Test
    fun `replaces an empty data directory`() {
        directory.createDirectories()
        legacy.createDirectories().resolve("config.yml").writeText("legacy")

        LegacyDirectoryMigrator.migrate(directory)

        assertFalse(legacy.exists())
        assertEquals("legacy", directory.resolve("config.yml").readText())
    }

    @Test
    fun `keeps both directories when the data directory is already in use`() {
        directory.createDirectories().resolve("config.yml").writeText("current")
        legacy.createDirectories().resolve("config.yml").writeText("legacy")

        LegacyDirectoryMigrator.migrate(directory)

        assertTrue(legacy.exists())
        assertEquals("current", directory.resolve("config.yml").readText())
    }
}
