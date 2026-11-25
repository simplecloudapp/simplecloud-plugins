package app.simplecloud.plugin.command.shared.command

import app.simplecloud.api.testing.TestCloud
import app.simplecloud.plugin.command.shared.CommandPlugin
import java.nio.file.Files

class TestCommands(cloud: TestCloud) {

    private val plugin = CommandPlugin(Files.createTempDirectory("cloud-command").toFile(), cloud.api())
    private val manager = TestCommandManager()
    private val sender = TestCommandSender()

    fun register() {
        CloudCommand(manager, plugin).register()
    }

    fun execute(command: String): List<String> {
        sender.clear()
        runCatching { manager.commandExecutor().executeCommand(sender, command).join() }
        return sender.plainMessages()
    }

    fun shutdown() {
        plugin.shutdown()
    }
}
