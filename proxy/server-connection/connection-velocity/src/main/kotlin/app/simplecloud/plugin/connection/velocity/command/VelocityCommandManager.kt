package app.simplecloud.plugin.connection.velocity.command

import app.simplecloud.plugin.connection.shared.command.ConnectionCommandManager
import app.simplecloud.plugin.connection.shared.config.CommandEntry
import app.simplecloud.plugin.connection.velocity.VelocityConnectionPlugin
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.proxy.ProxyServer
import java.util.concurrent.CopyOnWriteArrayList

class VelocityCommandManager(
    private val plugin: VelocityConnectionPlugin,
    private val proxy: ProxyServer,
) : ConnectionCommandManager {

    private val registeredCommands = CopyOnWriteArrayList<CommandMeta>()

    override fun register(commands: List<CommandEntry>) {
        commands.forEach { command -> register(command) }
    }

    override fun unregister() {
        registeredCommands.forEach { meta -> proxy.commandManager.unregister(meta) }
        registeredCommands.clear()
    }

    private fun register(command: CommandEntry) {
        val meta = proxy.commandManager.metaBuilder(command.name)
            .aliases(*command.aliases.toTypedArray())
            .plugin(plugin)
            .build()

        proxy.commandManager.register(meta, ConnectionCommand(plugin, proxy, command))
        registeredCommands.add(meta)
    }
}
