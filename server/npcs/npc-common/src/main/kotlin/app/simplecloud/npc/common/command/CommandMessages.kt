package app.simplecloud.npc.common.command

import app.simplecloud.npc.common.manager.NpcFailure
import app.simplecloud.npc.common.manager.NpcOperationResult
import app.simplecloud.npc.common.text.Msg
import app.simplecloud.npc.common.text.PlayerMessages
import app.simplecloud.npc.common.text.substitute
import app.simplecloud.npc.core.NpcLog
import app.simplecloud.npc.core.config.ActionFields
import app.simplecloud.npc.core.platform.NpcCommandSender
import app.simplecloud.npc.core.text.NpcPalette
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Path
import java.util.logging.Level
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

enum class NpcSuccessMessage(val path: String, val verb: String) {
    CREATED("command.npc.create.success", "Created"),
    DELETED("command.npc.delete.success", "Deleted"),
    UPDATED("command.npc.update.success", "Updated"),
    LINKED("command.npc.link.success", "Linked"),
    UNLINKED("command.npc.unlink.success", "Unlinked"),
    DUPLICATED("command.npc.duplicate.success", "Duplicated"),
}

object CommandMessages {
    val helpTopics = listOf("create", "link", "edit", "target", "hologram", "action", "pushback", "admin", "inventory")

    private const val CMD = "/$COMMAND_LABEL"
    private val ARGUMENT_START = Regex(""" [<\[]""")
    private const val CHAT_WIDTH = 320
    private const val HELP_INDENT = 8
    private const val HELP_GAP = 12

    @Volatile
    private var messagesPath: Path? = null

    private class Loaded(val root: ConfigurationNode, val variables: Map<String, String>)

    @Volatile
    private var loaded: Loaded? = null

    fun resourcePaths(): Set<String> = buildSet {
        addAll(FIXED_PATHS)
        addAll(PlayerMessages.PATHS)
        NpcSuccessMessage.entries.forEach { add(it.path) }
        NpcFailure.entries.forEach { add(failure(it).first) }
    }

    private const val DEFAULT_PREFIX = Msg.PREFIX
    private val OLD_DEFAULT_PREFIXES = listOf(
        "<#38bdf8><b>⚡</b>",
        "<#0EA5E9>⚡ NPC <#475569>|",
        "<#0EA5E9><bold>NPCs</bold> <#475569>|",
        "<#0EA5E9>SimpleCloud NPC <#475569>|",
    )

    private val FIXED_PATHS = setOf(
        "command.help.title",
        "command.help.topic-title",
        "command.help.unknown",
        "command.reload.success",
        "command.reload.failed",
    )

    fun initialize(path: Path) {
        messagesPath = path
        runCatching { updateDefaultPrefix(path) }
            .onFailure { NpcLog.logger.log(Level.WARNING, "Could not update the prefix in messages.yml", it) }
        reload()
    }

    internal fun updateDefaultPrefix(path: Path) {
        if (!path.exists()) return
        val text = path.readText()
        val updated = OLD_DEFAULT_PREFIXES.fold(text) { acc, old ->
            acc.replace("prefix: '$old'", "prefix: '$DEFAULT_PREFIX'")
        }
        if (updated != text) path.writeText(updated)
    }

    fun reload(): Boolean {
        val path = messagesPath ?: return false

        return runCatching {
            val root = YamlConfigurationLoader.builder().path(path).build().load()
            val variables = root.node("variables").childrenMap().mapNotNull { (key, node) ->
                node.string?.let { key.toString() to it }
            }.toMap()
            val palette = root.node("palette").childrenMap().mapNotNull { (key, node) ->
                val value = node.string ?: return@mapNotNull null
                key.toString() to value
            }.toMap()
            NpcPalette.update(palette)
            loaded = Loaded(root, variables)
        }.onFailure { NpcLog.logger.log(Level.WARNING, "Could not load messages.yml", it) }.isSuccess
    }

    fun send(
        sender: NpcCommandSender,
        path: String,
        fallback: String,
        replacements: Map<String, Any?> = emptyMap(),
    ) {
        sender.sendMessage(Msg.miniMessage.deserialize(render(path, fallback, replacements)))
    }

    private fun render(path: String, fallback: String, replacements: Map<String, Any?> = emptyMap()): String {
        val current = loaded
        val configured = current?.root?.node(*path.split('.').toTypedArray())?.string ?: fallback

        val escaped = replacements.mapValues { Msg.miniMessage.escapeTags(it.value?.toString().orEmpty()) }

        return configured.substitute((current?.variables.orEmpty() + escaped).mapKeys { "<${it.key}>" })
    }

    fun sendResult(
        sender: NpcCommandSender,
        result: NpcOperationResult,
        success: NpcSuccessMessage = NpcSuccessMessage.UPDATED,
    ) {
        when (result) {
            is NpcOperationResult.Success -> send(
                sender,
                success.path,
                "${Msg.successPrefix()}${success.verb} NPC <id>.",
                mapOf("id" to result.config.id),
            )

            is NpcOperationResult.Failure -> sender.sendMessage(Msg.miniMessage.deserialize(failureMessage(result)))
        }
    }

    fun sendHelp(sender: NpcCommandSender, topic: String? = null) {
        val normalizedTopic = topic?.lowercase()
        val fallbackLines = helpPage(normalizedTopic)
        if (fallbackLines == null) {
            send(
                sender,
                "command.help.unknown",
                "${Msg.errorPrefix()}There is no help topic like that. Try one of <topics>.",
                mapOf("topics" to helpTopics.joinToString()),
            )
            return
        }

        val page = normalizedTopic ?: "main"
        val title = if (normalizedTopic == null) {
            render("command.help.title", "${Msg.infoPrefix()}NPC commands  ${Msg.SUBTLE}$CMD")
        } else {
            render(
                "command.help.topic-title",
                "${Msg.infoPrefix()}NPC <topic> commands  ${Msg.SUBTLE}$CMD",
                mapOf("topic" to normalizedTopic),
            )
        }

        val lines = listOf(title) + renderLines("command.help.pages.$page", renderHelp(fallbackLines))

        sender.sendMessage(Msg.miniMessage.deserialize(lines.joinToString("\n")))
    }

    private fun renderLines(path: String, fallback: List<String>): List<String> {
        val current = loaded
        val configured = current?.root?.node(*path.split('.').toTypedArray())
            ?.childrenList()
            ?.mapNotNull(ConfigurationNode::getString)
            .orEmpty()
            .ifEmpty { fallback }

        val variables = current?.variables.orEmpty().mapKeys { "<${it.key}>" }

        return configured.map { it.substitute(variables) }
    }

    private fun helpPage(topic: String?): List<HelpLine>? = when (topic) {
        null -> listOf(
            command("create <id> <target>", "Create an NPC where you stand"),
            command("link <id> <provider> <reference> <target>", "Attach an NPC of another plugin"),
            command("edit [id]", "Open the editor"),
            command("info [id]", "Show the details of an NPC"),
            command("list", "List all NPCs"),
            command("delete [id]", "Delete an NPC"),
            command("help <topic>", "More commands by topic", "Topics: ${helpTopics.joinToString()}"),
        )

        "create" -> listOf(
            command("create <id> <target>", "Create an NPC where you stand"),
            note("Spawns a native NPC, links it to the target and opens its editor."),
            note("Example: $CMD create lobby Lobby"),
        )

        "link" -> listOf(
            command("link <id> <provider> <reference> <target>", "Attach an NPC of another plugin"),
            command("unlink <id>", "Detach it again, the NPC stays"),
            note("Works with Citizens, FancyNpcs, ZNPCsPlus and MythicMobs. The NPC itself"),
            note("is never changed. Example: $CMD link lobby citizens 42 Lobby"),
        )

        "edit" -> listOf(
            command("edit", "Edit the NPC you are looking at"),
            command("edit <id> target", "Change the targets"),
            command("edit <id> hologram", "Change the hologram"),
            command("edit <id> action", "Change the click actions"),
            command("edit <id> pushback", "Change the pushback"),
            command("edit <id> teleport", "Move it to where you stand"),
            command("edit <id> skin <player>", "Use the skin of a player"),
        )

        "target" -> listOf(
            command("edit <id> target list", "List the targets"),
            command("edit <id> target add <target>", "Add a target"),
            command("edit <id> target remove <target>", "Remove a target"),
        )

        "hologram" -> listOf(
            command("edit <id> hologram enable", "Show the hologram"),
            command("edit <id> hologram disable", "Hide the hologram"),
            command("edit <id> hologram height <value>", "Set its height"),
            command("edit <id> hologram line add <state> <text>", "Add a line"),
            command("edit <id> hologram line set <state> <line> <text>", "Change a line"),
            command("edit <id> hologram line remove <state> <line>", "Remove a line"),
        )

        "action" -> listOf(
            command("edit <id> action <type> <interaction> <state> [value]", "Set an action"),
            command("edit <id> action clear <interaction> <state> [field]", "Clear an action"),
            note("Types: join-target, open-inventory, play-sound, send-message,"),
            note("execute-command, teleport-here, title, subtitle, title-duration,"),
            note("action-bar, send-to-server, transfer-to-server"),
            note("Fields: ${ActionFields.NAMES.joinToString()}"),
        )

        "pushback" -> listOf(
            command("edit <id> pushback enable", "Push players away"),
            command("edit <id> pushback disable", "Stop pushing players away"),
            command("edit <id> pushback radius <value>", "Set the radius"),
            command("edit <id> pushback strength <value>", "Set the strength"),
            command("edit <id> pushback vertical <value>", "Set the upward push"),
            command("edit <id> pushback sound <sound>", "Play a sound on push"),
            command("edit <id> pushback sound clear", "Remove the sound"),
        )

        "admin" -> listOf(
            command("list", "List all NPCs"),
            command("info [id]", "Show the details of an NPC"),
            command("tp <id>", "Teleport to an NPC"),
            command("duplicate <id> <newId>", "Copy an NPC to where you stand"),
            command("delete [id]", "Delete an NPC"),
            command("reload", "Reload the configuration"),
            command("placeholders", "List the placeholders"),
        )

        "inventory" -> listOf(
            command("inventory create", "Create a menu step by step"),
            command("inventory create <id> <rows>", "Create a menu"),
            command("inventory edit <id>", "Open the menu editor"),
            command("inventory delete <id>", "Delete a menu"),
            command("inventory open <id> [player]", "Open a menu"),
            command("inventory list", "List all menus"),
            note("Open a menu from an NPC with $CMD edit <id> action open-inventory."),
        )

        else -> null
    }

    private fun command(usage: String, description: String, details: String = "") = HelpLine.Command(usage, description, details)

    private fun note(text: String) = HelpLine.Note(text)

    private fun renderHelp(lines: List<HelpLine>): List<String> {
        val commands = lines.filterIsInstance<HelpLine.Command>()
        val column = commands.filter { fits(it, width(it)) }.maxOfOrNull(::width) ?: 0

        return lines.map { line ->
            when (line) {
                is HelpLine.Command -> commandLine(line, column)
                is HelpLine.Note -> "  ${Msg.MUTED}${Msg.miniMessage.escapeTags(line.text)}"
            }
        }
    }

    private fun commandLine(command: HelpLine.Command, column: Int): String {
        val split = ARGUMENT_START.find(command.usage)?.range?.first ?: command.usage.length
        val literal = command.usage.substring(0, split)
        val arguments = command.usage.substring(split).trim()
        val inline = width(command) <= column && fits(command, column)
        val padding = if (inline) ChatPixels.spaces(column - width(command) + HELP_GAP) else ""
        val description = if (inline) "${Msg.MUTED}${esc(command.description)}" else ""
        val tip = listOfNotNull(
            "${Msg.HIGHLIGHT}${esc(command.description)}",
            if (command.details.isEmpty()) null else "${Msg.MUTED}${esc(command.details)}",
            "${Msg.MUTED}Usage: ${Msg.HIGHLIGHT}${esc("$CMD ${command.usage}")}",
            "",
            "${Msg.INTERACTIVE}Click to type this command",
        ).joinToString("\n")
        val line = "  ${Msg.INTERACTIVE}${esc(literal)} ${Msg.MUTED}${esc(arguments)}$padding$description"

        return "<click:suggest_command:'${arg("$CMD $literal ")}'><hover:show_text:'${arg(tip)}'>$line</hover></click>"
    }

    private fun width(command: HelpLine.Command): Int {
        val split = ARGUMENT_START.find(command.usage)?.range?.first ?: command.usage.length
        return ChatPixels.width(command.usage.substring(0, split) + " " + command.usage.substring(split).trim())
    }

    private fun fits(command: HelpLine.Command, column: Int): Boolean =
        HELP_INDENT + column + HELP_GAP + ChatPixels.width(command.description) <= CHAT_WIDTH

    private fun esc(text: String) = Msg.miniMessage.escapeTags(text)

    private fun arg(text: String) = text.replace("\\", "\\\\").replace("'", "\\'")

    private fun failure(failure: NpcFailure): Pair<String, String> = when (failure) {
        NpcFailure.INVALID_ID ->
            "command.npc.error.invalid-id" to
                "${Msg.errorPrefix()}<detail> is not a valid NPC id."

        NpcFailure.ALREADY_EXISTS ->
            "command.npc.error.already-exists" to
                "${Msg.errorPrefix()}NPC <detail> already exists."

        NpcFailure.NOT_FOUND ->
            "command.npc.error.not-found" to
                "${Msg.errorPrefix()}NPC <detail> was not found."

        NpcFailure.TARGET_NOT_FOUND ->
            "command.target.error.not-found" to
                "${Msg.errorPrefix()}Target <detail> was not found."

        NpcFailure.TARGET_AMBIGUOUS ->
            "command.target.error.ambiguous" to
                "${Msg.errorPrefix()}Target <detail> is both a group and a persistent server. " +
                "Rename one of them."

        NpcFailure.TARGET_ALREADY_USED ->
            "command.target.error.already-used" to
                "${Msg.errorPrefix()}This NPC already uses target <detail>."

        NpcFailure.TARGET_LIMIT ->
            "command.target.error.limit" to
                "${Msg.errorPrefix()}This NPC already has the most targets it can have (<detail>)."

        NpcFailure.CREATE_FAILED ->
            "command.npc.create.failed" to
                "${Msg.errorPrefix()}Couldn't create the NPC<detail-suffix>"

        NpcFailure.PROVIDER_UNAVAILABLE ->
            "command.provider.error.unavailable" to
                "${Msg.errorPrefix()}Provider <detail> is not available."

        NpcFailure.PROVIDER_NPC_NOT_FOUND ->
            "command.provider.error.external-npc-missing" to
                "${Msg.errorPrefix()}The provider has no NPC <detail>."

        NpcFailure.PROVIDER_NPC_ALREADY_LINKED ->
            "command.provider.error.already-linked" to
                "${Msg.errorPrefix()}Provider NPC <detail> is already linked."

        NpcFailure.NOT_LINKED ->
            "command.npc.error.not-linked" to
                "${Msg.errorPrefix()}NPC <detail> is not linked. Use delete instead."
    }

    private fun failureMessage(result: NpcOperationResult.Failure): String {
        val detail = result.detail.orEmpty()
        val (path, fallback) = failure(result.failure)
        val suffix = if (detail.isBlank()) "." else ": $detail"

        return render(path, fallback, mapOf("detail" to detail, "detail-suffix" to suffix))
    }
}
