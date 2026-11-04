package app.simplecloud.plugin.signs.shared.repository

import app.simplecloud.plugin.signs.shared.config.layout.LayoutConfig
import app.simplecloud.plugin.signs.shared.config.layout.LayoutType
import app.simplecloud.plugin.signs.shared.config.layout.MatcherOperation
import app.simplecloud.plugin.signs.shared.config.layout.MatcherType
import app.simplecloud.plugin.signs.shared.config.location.SignLocation
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class YamlFormatTest {

    private val directory: Path = createTempDirectory("signs")

    @Test
    fun `loads a layout written by the old plugin with a byte order mark`() {
        directory.resolve("layouts").toFile().mkdirs()
        directory.resolve("layouts/online.yml").writeText(
            "\uFEFF" +
                """
                name: 'online_default'
                rule: 'ONLINE'
                type: GROUP
                priority: 9
                server-name: '%group%-%numerical-id%'
                behind-block: 'LIME_CONCRETE'
                frame-update-interval: 1250
                matcher:
                  MATCH_ANY:
                    - operation: STARTS_WITH
                      value: 'lobby'

                frames:
                  - lines:
                      - '<color:#22c55e>⚡</color>'
                      - '<group>-<numerical-id>'
                """.trimIndent(),
        )
        val layouts = YamlDirectory(directory.resolve("layouts"), LayoutConfig::class.java)

        layouts.load()

        val layout = layouts.findAll().single()
        assertEquals("online_default", layout.name)
        assertEquals("ONLINE", layout.rule)
        assertEquals(LayoutType.GROUP, layout.type)
        assertEquals(9, layout.priority)
        assertEquals("%group%-%numerical-id%", layout.serverName)
        assertEquals("LIME_CONCRETE", layout.behindBlock)
        assertEquals(1250, layout.frameUpdateInterval)
        assertEquals(MatcherOperation.STARTS_WITH, layout.matcher.getValue(MatcherType.MATCH_ANY).single().operation)
        assertEquals(listOf("<color:#22c55e>⚡</color>", "<group>-<numerical-id>"), layout.frames.single().lines)
        assertTrue(layout.nameMatches("lobby-1"))
        assertFalse(layout.nameMatches("bedwars-1"))
    }

    @Test
    fun `keeps the old locations format and file`() {
        val locationsDirectory = directory.resolve("locations")
        locationsDirectory.toFile().mkdirs()
        val file = locationsDirectory.resolve("lobby-signs.yml")
        file.writeText(
            """
            group: lobby
            locations:
            - world: world
              x: 1.0
              y: 64.0
              z: -3.0
            """.trimIndent(),
        )
        val locations = LocationRepository(locationsDirectory)
        locations.load()
        val added = SignLocation("world", 2.0, 64.0, -3.0)

        locations.addToGroup("lobby", added)
        locations.addToPersistentServer("smp", SignLocation("world", 5.0, 64.0, 5.0))

        assertEquals(listOf("lobby-signs.yml", "persistent-smp.yml"), files(locationsDirectory))
        assertTrue(locations.isRegistered(added))
        assertTrue("persistent-server: smp" in locationsDirectory.resolve("persistent-smp.yml").readText())

        val reloaded = LocationRepository(locationsDirectory)
        reloaded.load()
        assertEquals(
            listOf(SignLocation("world", 1.0, 64.0, -3.0), added),
            reloaded.find("lobby")?.locations,
        )

        assertEquals(2, reloaded.removeAll("lobby").size)
        assertFalse(reloaded.isRegistered(added))
    }

    private fun files(directory: Path): List<String> = Files.list(directory).use { paths ->
        paths.map { it.fileName.toString() }.sorted().toList()
    }
}
