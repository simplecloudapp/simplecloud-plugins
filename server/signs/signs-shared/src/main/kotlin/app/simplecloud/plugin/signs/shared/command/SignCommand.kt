package app.simplecloud.plugin.signs.shared.command

import app.simplecloud.plugin.core.shared.adventure.tags
import app.simplecloud.plugin.core.shared.command.CloudSender
import app.simplecloud.plugin.core.shared.command.CloudSuggestions
import app.simplecloud.plugin.core.shared.command.CommandExceptionHandler
import app.simplecloud.plugin.core.shared.command.HelpEntry
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.core.shared.controller.ApiUtils
import app.simplecloud.plugin.signs.shared.SignManager
import app.simplecloud.plugin.signs.shared.SignPermissions
import app.simplecloud.plugin.signs.shared.config.MessageConfig
import app.simplecloud.plugin.signs.shared.config.location.LocationsConfig
import app.simplecloud.plugin.signs.shared.config.location.SignLocation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.parser.standard.IntegerParser.integerParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider

class SignCommand<C : CloudSender>(
    private val manager: CommandManager<C>,
    private val signs: SignManager,
    private val messages: ConfigurationFactory<MessageConfig>,
    private val targetFinder: SignTargetFinder<C>,
) {

    private val config get() = messages.get()

    fun register() {
        val root = manager.commandBuilder(NAME, *ALIASES).permission(SignPermissions.BASE)
        manager.command(root.handler { sendHelp(it.sender()) })
        manager.command(root.literal("help").handler { sendHelp(it.sender()) })
        manager.command(
            root.literal("list")
                .optional("page", integerParser(1))
                .optional("target", stringParser(), registeredTargets())
                .permission(SignPermissions.LIST)
                .handler { list(it.sender(), it.getOrDefault("page", 1), it.getOrDefault("target", null as String?)) },
        )
        manager.command(
            root.literal("add").literal("group")
                .required("group", stringParser(), CloudSuggestions.serverGroups(signs.api))
                .permission(SignPermissions.ADD)
                .suspendingHandler(signs.scope) { addGroup(it.sender(), it.get("group")) },
        )
        manager.command(
            root.literal("add").literal("persistent")
                .required("persistentServer", stringParser(), CloudSuggestions.persistentServers(signs.api))
                .permission(SignPermissions.ADD)
                .suspendingHandler(signs.scope) { addPersistentServer(it.sender(), it.get("persistentServer")) },
        )
        manager.command(
            root.literal("remove")
                .permission(SignPermissions.REMOVE)
                .suspendingHandler(signs.scope) { remove(it.sender()) },
        )
        manager.command(
            root.literal("remove")
                .required("target", stringParser(), registeredTargets())
                .permission(SignPermissions.REMOVE_GROUP)
                .handler { removeAll(it.sender(), it.get("target")) },
        )
        CommandExceptionHandler(manager, messages).register()
    }

    private suspend fun addGroup(sender: C, name: String) {
        val location = findUnregisteredSign(sender) ?: return
        val group = ApiUtils.findGroup(signs.api, name)
        if (group == null) {
            send(sender, config.command.add.groupNotFound, tags("group" to name))
            return
        }
        signs.registerForGroup(group.name, location)
        send(sender, config.command.add.success, tags("target" to group.name))
    }

    private suspend fun addPersistentServer(sender: C, name: String) {
        val location = findUnregisteredSign(sender) ?: return
        val persistentServer = ApiUtils.findPersistentServer(signs.api, name)
        if (persistentServer == null) {
            send(sender, config.command.add.persistentServerNotFound, tags("persistent-server" to name))
            return
        }
        signs.registerForPersistentServer(persistentServer.name, location)
        send(sender, config.command.add.success, tags("target" to persistentServer.name))
    }

    private suspend fun remove(sender: C) {
        val location = findSign(sender) ?: return
        if (!signs.isRegistered(location)) {
            send(sender, config.command.remove.notRegistered)
            return
        }
        signs.remove(location)
        send(sender, config.command.remove.success)
    }

    private fun removeAll(sender: C, target: String) {
        val amount = signs.removeAll(target)
        val message = if (amount == 0) config.command.remove.noneRegistered else config.command.remove.allSuccess
        send(sender, message, tags("amount" to amount, "target" to target))
    }

    private fun list(sender: C, page: Int, target: String?) {
        val messages = config.command.list
        val entries = signs.configs()
            .filter { target == null || it.identifier() == target }
            .flatMap { config -> config.locations.map { displayName(config) to it } }
        if (entries.isEmpty()) {
            val message = if (target == null) messages.empty else config.command.remove.noneRegistered
            send(sender, message, tags("target" to target))
            return
        }

        val pages = (entries.size + PAGE_SIZE - 1) / PAGE_SIZE
        val current = page.coerceIn(1, pages)
        send(sender, messages.title, tags("amount" to entries.size))
        entries.drop((current - 1) * PAGE_SIZE).take(PAGE_SIZE).forEach { (name, location) ->
            send(sender, messages.entry, tags("target" to name), locationTags(location))
        }

        val targetArgument = if (target == null) "" else " $target"
        send(
            sender,
            messages.footer,
            tags("page" to current, "pages" to pages),
            pageButton("previous", messages.previous, "/$NAME list ${current - 1}$targetArgument", current > 1),
            pageButton("next", messages.next, "/$NAME list ${current + 1}$targetArgument", current < pages),
        )
    }

    private fun locationTags(location: SignLocation): TagResolver {
        val x = location.x.toInt()
        val y = location.y.toInt()
        val z = location.z.toInt()
        val hover = HoverEvent.showText(config.msg(config.command.list.teleportHover))
        return TagResolver.resolver(
            tags("world" to location.world, "x" to x, "y" to y, "z" to z),
            Placeholder.styling("teleport", ClickEvent.runCommand("/minecraft:tp @s $x $y $z"), hover),
        )
    }

    private fun pageButton(name: String, message: String, command: String, visible: Boolean): TagResolver {
        if (!visible) return Placeholder.component(name, Component.empty())
        return Placeholder.component(name, config.msg(message).clickEvent(ClickEvent.runCommand(command)))
    }

    private suspend fun findUnregisteredSign(sender: C): SignLocation? {
        val location = findSign(sender) ?: return null
        if (signs.isRegistered(location)) {
            send(sender, config.command.add.alreadyRegistered)
            return null
        }
        return location
    }

    private suspend fun findSign(sender: C): SignLocation? {
        if (!sender.isPlayer()) {
            send(sender, config.command.exception.invalidSender)
            return null
        }
        val location = targetFinder.find(sender)
        if (location == null) send(sender, config.command.notLookingAtSign)
        return location
    }

    private fun sendHelp(sender: C) {
        send(sender, config.command.help.title)
        HELP_ENTRIES.forEach {
            sender.sendMessage(HelpEntry.create(config, config.command.help.entry, config.command.help.hover, "/$NAME $it"))
        }
    }

    private fun registeredTargets(): SuggestionProvider<C> = SuggestionProvider.blocking { _, _ ->
        signs.configs().map { Suggestion.suggestion(it.identifier()) }
    }

    private fun displayName(config: LocationsConfig): String =
        if (config.isPersistentServer()) "persistent server ${config.persistentServer}" else config.identifier()

    private fun send(sender: C, message: String, vararg resolvers: TagResolver) {
        sender.sendMessage(config.msg(message, *resolvers))
    }

    companion object {
        private const val NAME = "simplecloudsign"
        private const val PAGE_SIZE = 5

        private val ALIASES = arrayOf("simplecloudsigns", "scsigns", "scsign", "cloudsigns", "cloudsign", "signs", "sign")
        private val HELP_ENTRIES = listOf(
            "add group <group>",
            "add persistent <name>",
            "remove [group|persistent server]",
            "list [page] [group|persistent server]",
        )
    }
}
