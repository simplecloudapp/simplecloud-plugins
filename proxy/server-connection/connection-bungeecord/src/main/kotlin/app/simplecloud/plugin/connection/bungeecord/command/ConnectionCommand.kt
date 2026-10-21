package app.simplecloud.plugin.connection.bungeecord.command

import app.simplecloud.plugin.connection.bungeecord.BungeeCordConnectionPlugin
import app.simplecloud.plugin.core.bungeecord.text.BungeeComponentConverter
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import org.slf4j.LoggerFactory

class ConnectionCommand(
    private val plugin: BungeeCordConnectionPlugin,
) : Command("connection", "simplecloud.connection.reload") {

    private val logger = LoggerFactory.getLogger(ConnectionCommand::class.java)

    override fun execute(sender: CommandSender, args: Array<out String>) {
        val messages = plugin.connectionPlugin.messageConfig.get()

        if (args.firstOrNull()?.equals("reload", ignoreCase = true) != true) {
            sendMessage(sender, messages.command.commandUsage)
            return
        }

        sendMessage(sender, messages.command.configReloading)
        try {
            plugin.reload()
            sendMessage(sender, messages.command.configReloadedSuccess)
        } catch (exception: Exception) {
            sendMessage(sender, messages.command.configReloadedFailed)
            logger.error("Failed to reload config", exception)
        }
    }

    private fun sendMessage(sender: CommandSender, rawMessage: String) {
        val messages = plugin.connectionPlugin.messageConfig.get()
        sender.sendMessage(BungeeComponentConverter.toBungee(messages.msg(rawMessage)))
    }
}
