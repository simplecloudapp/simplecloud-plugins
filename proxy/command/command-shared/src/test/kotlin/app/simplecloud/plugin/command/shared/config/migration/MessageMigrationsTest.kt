package app.simplecloud.plugin.command.shared.config.migration

import app.simplecloud.plugin.command.shared.config.HelpSection
import app.simplecloud.plugin.command.shared.config.MessageConfig
import app.simplecloud.plugin.command.shared.config.UsageSection
import app.simplecloud.core.shared.config.YamlFileConfigurator
import java.io.File
import java.nio.file.Files
import kotlin.test.*

class MessageMigrationsTest {

    private val directory = Files.createTempDirectory("command-messages").toFile()
    private val file = File(directory, "messages.yml")

    @Test
    fun `renamed help entries keep their custom description`() {
        file.writeText(
            """
            version: 1
            command:
              help:
                descriptions:
                  'group stop <group> [id]': 'Custom stop'
                  'player send <player> <group|ps> <target>': 'Custom send'
            """.trimIndent(),
        )

        val descriptions = load().command.help.descriptions

        assertEquals("Custom stop", descriptions["group stop <group> [ids]"])
        assertEquals("Custom send", descriptions["player send <player> <group|ps|server> <target> [mode]"])
        assertTrue("group stop <group> [id]" !in descriptions)
    }

    @Test
    fun `missing help entries are added`() {
        file.writeText(
            """
            version: 1
            command:
              help:
                descriptions:
                  'reload': 'Custom reload'
            """.trimIndent(),
        )

        val descriptions = load().command.help.descriptions

        assertEquals("Custom reload", descriptions["reload"])
        assertEquals(HelpSection().descriptions.keys, descriptions.keys)
    }

    @Test
    fun `old defaults are replaced and custom messages are kept`() {
        file.writeText(
            """
            version: 1
            command:
              usage:
                invalid-target-type: '<prefix> <error>Use group or ps as the target type.'
              group:
                stop:
                  success: 'Custom stop message'
            """.trimIndent(),
        )

        val config = load()

        assertEquals(UsageSection().invalidTargetType, config.command.usage.invalidTargetType)
        assertEquals("Custom stop message", config.command.group.stop.success)
        assertEquals(2, config.version)
        assertTrue(File(directory, "messages.yml.v1.bak").exists())
    }

    private fun load(): MessageConfig =
        checkNotNull(YamlFileConfigurator(MessageConfig::class.java, MessageMigrations).load(file))
}
