package app.simplecloud.plugin.proxy.shared.command.commands

import app.simplecloud.api.CloudApi
import app.simplecloud.plugin.core.shared.command.CloudSender
import app.simplecloud.plugin.core.shared.command.CloudSuggestions
import app.simplecloud.plugin.proxy.shared.ProxyEssentials
import app.simplecloud.plugin.proxy.shared.utilities.CommandPermissions
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.incendo.cloud.CommandManager
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.permission.Permission
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider
import org.slf4j.LoggerFactory

class LayoutCommand<C : CloudSender>(
    private val api: CloudApi,
    private val plugin: ProxyEssentials,
    private val manager: CommandManager<C>,
) {

    private val logger = LoggerFactory.getLogger(LayoutCommand::class.java)

    fun register() {
        registerHelp()
        registerInfo()
        registerSet()
    }

    private fun registerHelp() {
        manager.command(
            manager.commandBuilder("scproxy")
                .literal("layout")
                .handler { context -> sendHelp(context) }
                .permission(Permission.permission(CommandPermissions.LAYOUT_HELP))
                .build(),
        )
        manager.command(
            manager.commandBuilder("scproxy")
                .literal("layout")
                .literal("help")
                .handler { context -> sendHelp(context) }
                .permission(Permission.permission(CommandPermissions.LAYOUT_HELP))
                .build(),
        )
    }

    private fun registerInfo() {
        manager.command(
            manager.commandBuilder("scproxy")
                .literal("layout")
                .literal("info")
                .handler { context -> sendInfo(context.sender(), "default", plugin.config.get().initialLayout) }
                .permission(Permission.permission(CommandPermissions.LAYOUT_INFO))
                .build(),
        )
        manager.command(
            manager.commandBuilder("scproxy")
                .literal("layout")
                .literal("info")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .suspendingHandler { context ->
                    val sender = context.sender()
                    val groupName = context.get<String>("group")
                    try {
                        if (!groupExists(groupName)) {
                            sender.sendMessage(groupNotFound(groupName))
                            return@suspendingHandler
                        }

                        val config = plugin.config.get()
                        val state = plugin.joinStateService.getGroupState(groupName)
                        val forcedLayout = config.joinstates.find { it.name == state }?.forcedMotdLayout
                        val layout =
                            forcedLayout ?: plugin.layoutService.getGroupLayout(groupName) ?: config.initialLayout
                        sendInfo(sender, groupName, layout)
                    } catch (e: Exception) {
                        logger.error("Could not load the layout of group '$groupName'", e)
                        sender.sendMessage(plugin.messageFormatter.format(plugin.messages.get().command.failure))
                    }
                }
                .permission(Permission.permission(CommandPermissions.LAYOUT_INFO))
                .build(),
        )
    }

    private fun registerSet() {
        manager.command(
            manager.commandBuilder("scproxy")
                .literal("layout")
                .literal("set")
                .required("group", stringParser(), CloudSuggestions.groups(api))
                .required(
                    "layout",
                    stringParser(),
                    SuggestionProvider.blocking { _, _ ->
                        plugin.layoutRepository.getNames().map { Suggestion.suggestion(it) }
                    },
                )
                .suspendingHandler { context ->
                    val sender = context.sender()
                    val groupName = context.get<String>("group")
                    val layout = context.get<String>("layout")
                    val messages = plugin.messages.get().command.layout.set

                    if (plugin.layoutRepository.find(layout) == null) {
                        sender.sendMessage(plugin.messageFormatter.format(messages.updateFailure))
                        return@suspendingHandler
                    }

                    try {
                        if (!groupExists(groupName)) {
                            sender.sendMessage(groupNotFound(groupName))
                            return@suspendingHandler
                        }

                        if (plugin.layoutService.getGroupLayout(groupName) == layout) {
                            sender.sendMessage(plugin.messageFormatter.format(messages.updateNoChange))
                            return@suspendingHandler
                        }

                        plugin.layoutService.setGroupLayout(groupName, layout)
                        sender.sendMessage(plugin.messageFormatter.format(messages.updateSuccess))
                    } catch (e: Exception) {
                        logger.error("Could not set the layout of group '$groupName' to '$layout'", e)
                        sender.sendMessage(plugin.messageFormatter.format(messages.updateFailure))
                    }
                }
                .permission(Permission.permission(CommandPermissions.LAYOUT_SET))
                .build(),
        )
    }

    private fun sendHelp(context: CommandContext<C>) {
        val sender = context.sender()
        val help = plugin.messages.get().command.layout.help
        val commands = listOf(
            "/scproxy layout help",
            "/scproxy layout info [group]",
            "/scproxy layout set <group> <layout>",
        )

        sender.sendMessage(plugin.messageFormatter.format(help.header))
        commands.forEach {
            sender.sendMessage(plugin.messageFormatter.formatHelpEntry(help, it))
        }
    }

    private suspend fun groupExists(groupName: String): Boolean = api.group().findGroupByName(groupName).await().isPresent

    private fun groupNotFound(groupName: String) = plugin.messageFormatter.format(
        plugin.messages.get().command.targetNotFound,
        Placeholder.unparsed("target", groupName),
    )

    private fun sendInfo(sender: C, groupName: String, layout: String) {
        val info = plugin.messages.get().command.layout.info
        sender.sendMessage(plugin.messageFormatter.format(info.header, Placeholder.unparsed("group", groupName)))
        sender.sendMessage(plugin.messageFormatter.format(info.entry, Placeholder.unparsed("layout", layout)))
    }
}
