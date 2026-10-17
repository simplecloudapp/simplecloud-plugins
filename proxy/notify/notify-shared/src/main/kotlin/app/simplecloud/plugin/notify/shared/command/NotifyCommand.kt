package app.simplecloud.plugin.notify.shared.command

import app.simplecloud.api.player.CloudPlayer
import app.simplecloud.plugin.notify.shared.NotifyPlugin
import app.simplecloud.plugin.notify.shared.notification.NotificationType
import app.simplecloud.plugin.notify.shared.utilities.NotifyPermissions
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.parser.standard.BooleanParser
import org.incendo.cloud.parser.standard.StringParser
import org.slf4j.LoggerFactory
import java.util.UUID

class NotifyCommand<C : CloudSender>(
    private val manager: CommandManager<C>,
    private val notify: NotifyPlugin
) {

    private val logger = LoggerFactory.getLogger(NotifyCommand::class.java)
    private val messages get() = notify.messages.get()
    private val formatter = notify.formatter
    private val cache = notify.cache

    private val commands = linkedMapOf(
        "/scnotify help" to NotifyPermissions.COMMAND_HELP,
        "/scnotify reload config" to NotifyPermissions.COMMAND_RELOAD_CONFIG,
        "/scnotify list" to NotifyPermissions.COMMAND_TOGGLE_SELF,
        "/scnotify <enable/disable> [type]" to NotifyPermissions.COMMAND_TOGGLE_SELF,
        "/scnotify set <player> <true/false> [type]" to NotifyPermissions.COMMAND_TOGGLE_OTHER,
    )

    fun register() {
        registerHelp()
        registerReload()
        registerList()
        registerToggleSelf()
        registerToggleOther()
    }

    private fun registerHelp() {
        val root = manager.commandBuilder("scnotify").permission(NotifyPermissions.COMMAND_HELP)
        manager.command(root.handler { context -> sendHelp(context.sender()) })
        manager.command(root.literal("help").handler { context -> sendHelp(context.sender()) })
    }

    private fun registerReload() {
        manager.command(
            manager.commandBuilder("scnotify")
                .literal("reload").literal("config")
                .permission(NotifyPermissions.COMMAND_RELOAD_CONFIG)
                .handler { context ->
                    val message = if (notify.reload()) messages.command.reload.config.success else messages.command.reload.config.failed
                    context.sender().sendMessage(formatter.message(message))
                }
        )
    }

    private fun registerList() {
        manager.command(
            manager.commandBuilder("scnotify")
                .literal("list")
                .permission(NotifyPermissions.COMMAND_TOGGLE_SELF)
                .suspendingHandler(notify.scope) { context -> sendList(context.sender()) }
        )
    }

    private fun registerToggleSelf() {
        val root = manager.commandBuilder("scnotify").permission(NotifyPermissions.COMMAND_TOGGLE_SELF)

        manager.command(
            root.literal("enable")
                .optional("type", StringParser.stringParser(), NotifySuggestions.receivableTypes())
                .suspendingHandler(notify.scope) { context ->
                    toggleSelf(context.sender(), true, context.optional<String>("type").orElse(null))
                }
        )

        manager.command(
            root.literal("disable")
                .optional("type", StringParser.stringParser(), NotifySuggestions.receivableTypes())
                .suspendingHandler(notify.scope) { context ->
                    toggleSelf(context.sender(), false, context.optional<String>("type").orElse(null))
                }
        )
    }

    private fun registerToggleOther() {
        manager.command(
            manager.commandBuilder("scnotify")
                .literal("set")
                .required("player", StringParser.stringParser())
                .required("value", BooleanParser.booleanParser())
                .optional("type", StringParser.stringParser(), NotifySuggestions.types())
                .permission(NotifyPermissions.COMMAND_TOGGLE_OTHER)
                .suspendingHandler(notify.scope) { context ->
                    toggleOther(
                        context.sender(),
                        context.get("player"),
                        context.get("value"),
                        context.optional<String>("type").orElse(null)
                    )
                }
        )
    }

    private fun sendHelp(sender: C) {
        val usable = commands.filterValues { permission -> sender.hasPermission(permission) }.keys

        if (usable.isEmpty()) {
            sender.sendMessage(formatter.message(messages.command.help.empty))
            return
        }

        sender.sendMessage(formatter.message(messages.command.help.title))
        usable.forEach { usage ->
            sender.sendMessage(formatter.message(messages.command.help.entry, Placeholder.unparsed("command", usage)))
        }
    }

    private suspend fun sendList(sender: C) {
        val id = sender.getUniqueId()

        if (id == null) {
            sender.sendMessage(formatter.message(messages.command.error.onlyPlayers))
            return
        }

        val disabled = fetchDisabledTypes(sender, id) ?: return
        val types = NotificationType.entries.filter { type -> type.canReceive(sender) }
        val listMessages = messages.command.list

        if (types.isEmpty()) {
            sender.sendMessage(formatter.message(listMessages.empty))
            return
        }

        sender.sendMessage(formatter.message(listMessages.title))
        types.forEach { type ->
            sender.sendMessage(listEntry(type, type !in disabled))
        }
    }

    private fun listEntry(type: NotificationType, enabled: Boolean): Component {
        val listMessages = messages.command.list
        val command = "/scnotify ${if (enabled) "disable" else "enable"} ${type.key}"

        return formatter.message(
            listMessages.entry,
            typePlaceholder(type),
            Placeholder.parsed("state", if (enabled) listMessages.enabled else listMessages.disabled),
            Placeholder.styling("toggle", ClickEvent.runCommand(command))
        )
    }

    private suspend fun toggleSelf(sender: C, value: Boolean, input: String?) {
        val id = sender.getUniqueId()

        if (id == null) {
            sender.sendMessage(formatter.message(messages.command.error.onlyPlayers))
            return
        }

        val type = if (input == null) null else NotificationType.fromKey(input)

        if (input != null && (type == null || !type.canReceive(sender))) {
            sender.sendMessage(unknownType(input))
            return
        }

        val types = typesOf(type).filter { receivable -> receivable.canReceive(sender) }
        val disabled = fetchDisabledTypes(sender, id) ?: return

        if (isAlreadySet(types, disabled, value)) {
            sender.sendMessage(ownMessage(type, value, true))
            return
        }

        if (!updateTypes(sender, id, types, value)) return
        sender.sendMessage(ownMessage(type, value, false))
    }

    private suspend fun toggleOther(sender: C, playerName: String, value: Boolean, input: String?) {
        val type = if (input == null) null else NotificationType.fromKey(input)

        if (input != null && type == null) {
            sender.sendMessage(unknownType(input))
            return
        }

        val target = try {
            notify.api.player().get(playerName).await()
        } catch (e: Exception) {
            logger.error("Failed to load player '$playerName'", e)
            sender.sendMessage(formatter.message(messages.command.error.storageUnavailable))
            return
        }

        if (target == null) {
            sender.sendMessage(playerError(messages.command.error.playerNotFound, playerName))
            return
        }

        if (!target.isOnline) {
            sender.sendMessage(playerError(messages.command.error.playerNotOnline, playerName))
            return
        }

        val types = typesOf(type)
        val disabled = fetchDisabledTypes(sender, target.uniqueId) ?: return

        if (isAlreadySet(types, disabled, value)) {
            sender.sendMessage(targetMessage(type, value, true, target))
            return
        }

        if (!updateTypes(sender, target.uniqueId, types, value)) return

        sender.sendMessage(targetMessage(type, value, false, target))
        target.sendMessage(ownMessage(type, value, false))
    }

    private fun typesOf(type: NotificationType?): List<NotificationType> {
        if (type == null) return NotificationType.entries
        return listOf(type)
    }

    private fun isAlreadySet(types: List<NotificationType>, disabled: Set<NotificationType>, value: Boolean): Boolean {
        return types.all { type -> (type !in disabled) == value }
    }

    private suspend fun fetchDisabledTypes(sender: C, id: UUID): Set<NotificationType>? {
        try {
            return cache.fetchDisabledTypes(id)
        } catch (e: Exception) {
            logger.error("Failed to load the notify settings of '$id'", e)
            sender.sendMessage(formatter.message(messages.command.error.storageUnavailable))
            return null
        }
    }

    private suspend fun updateTypes(sender: C, id: UUID, types: List<NotificationType>, value: Boolean): Boolean {
        try {
            cache.setEnabled(id, types, value)
            return true
        } catch (e: Exception) {
            logger.error("Failed to update the notify settings of '$id'", e)
            sender.sendMessage(formatter.message(messages.command.error.storageUnavailable))
            return false
        }
    }

    private fun ownMessage(type: NotificationType?, value: Boolean, already: Boolean): Component {
        if (type == null) {
            val notifyMessages = messages.command.notify
            return formatter.message(
                when {
                    already && value -> notifyMessages.alreadyEnabled
                    already -> notifyMessages.alreadyDisabled
                    value -> notifyMessages.enabled
                    else -> notifyMessages.disabled
                }
            )
        }

        val typeMessages = messages.command.notify.type
        return formatter.message(
            when {
                already && value -> typeMessages.alreadyEnabled
                already -> typeMessages.alreadyDisabled
                value -> typeMessages.enabled
                else -> typeMessages.disabled
            },
            typePlaceholder(type)
        )
    }

    private fun targetMessage(type: NotificationType?, value: Boolean, already: Boolean, target: CloudPlayer): Component {
        if (type == null) {
            val setMessages = messages.command.set
            return playerMessage(
                when {
                    already && value -> setMessages.alreadyEnabled
                    already -> setMessages.alreadyDisabled
                    value -> setMessages.enabled
                    else -> setMessages.disabled
                },
                target
            )
        }

        val typeMessages = messages.command.set.type
        return playerMessage(
            when {
                already && value -> typeMessages.alreadyEnabled
                already -> typeMessages.alreadyDisabled
                value -> typeMessages.enabled
                else -> typeMessages.disabled
            },
            target,
            typePlaceholder(type)
        )
    }

    private fun typePlaceholder(type: NotificationType): TagResolver {
        return Placeholder.parsed("type", messages.notifications.types[type.key] ?: type.key)
    }

    private fun unknownType(input: String) = formatter.message(
        messages.command.error.unknownType,
        Placeholder.unparsed("type", input)
    )

    private fun playerMessage(message: String, target: CloudPlayer, vararg tagResolver: TagResolver) = formatter.message(
        message,
        Placeholder.parsed("playername", target.name),
        Placeholder.unparsed("displayname", target.displayName ?: target.name),
        *tagResolver
    )

    private fun playerError(message: String, playerName: String) = formatter.message(
        message,
        Placeholder.unparsed("playername", playerName)
    )

}
