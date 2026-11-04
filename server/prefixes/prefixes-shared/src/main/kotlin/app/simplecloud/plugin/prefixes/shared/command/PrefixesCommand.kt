package app.simplecloud.plugin.prefixes.shared.command

import app.simplecloud.plugin.core.shared.command.CloudSender
import app.simplecloud.plugin.core.shared.command.CommandExceptionHandler
import app.simplecloud.plugin.core.shared.command.HelpEntry
import app.simplecloud.plugin.prefixes.shared.Prefixes
import app.simplecloud.plugin.prefixes.shared.utilities.PrefixesPermissions
import org.incendo.cloud.CommandManager

class PrefixesCommand<C : CloudSender>(
    private val manager: CommandManager<C>,
    private val prefixes: Prefixes,
) {

    private val messages get() = prefixes.messages.get()

    fun register() {
        registerHelp()
        registerReload()
        CommandExceptionHandler(manager, prefixes.messages).register()
    }

    private fun registerHelp() {
        val root = manager.commandBuilder("scprefix").permission(PrefixesPermissions.COMMAND)
        manager.command(root.handler { sendHelp(it.sender()) })
        manager.command(root.literal("help").handler { sendHelp(it.sender()) })
    }

    private fun registerReload() {
        manager.command(
            manager.commandBuilder("scprefix")
                .literal("reload")
                .permission(PrefixesPermissions.RELOAD)
                .handler {
                    val message = if (prefixes.reload()) messages.command.reload.success else messages.command.reload.failed
                    it.sender().sendMessage(messages.msg(message))
                },
        )
    }

    private fun sendHelp(sender: C) {
        sender.sendMessage(messages.msg(messages.command.help.title))
        sender.sendMessage(HelpEntry.create(messages, messages.command.help.entry, messages.command.help.hover, "/scprefix help"))

        if (sender.hasPermission(PrefixesPermissions.RELOAD)) {
            sender.sendMessage(HelpEntry.create(messages, messages.command.help.entry, messages.command.help.hover, "/scprefix reload"))
        }
    }
}
