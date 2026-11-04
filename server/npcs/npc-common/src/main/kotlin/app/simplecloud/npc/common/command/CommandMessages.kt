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
            render("command.help.title", "${Msg.infoPrefix()}NPC commands")
        } else {
            render(
                "command.help.topic-title",
                "${Msg.infoPrefix()}NPC commands: <topic>",
                mapOf("topic" to normalizedTopic),
            )
        }

        val lines = listOf(title) + renderLines("command.help.pages.$page", fallbackLines)

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

    private fun helpPage(topic: String?): List<String>? = when (topic) {
        null -> listOf(
            command("$CMD create", "$CMD create", "Create an NPC"),
            command("$CMD link ", "$CMD link", "Attach an existing Citizens/FancyNpcs/ZNPCsPlus NPC"),
            command("$CMD edit", "$CMD edit [id]", "Edit an NPC (or the one you're looking at)"),
            command("$CMD list", "$CMD list", "List all NPCs"),
            command("$CMD info", "$CMD info [id]", "View details (or the one you're looking at)"),
            command("$CMD delete", "$CMD delete [id]", "Delete an NPC (or the one you're looking at)"),
            note("More: $CMD help <topic>"),
            note("Topics: ${helpTopics.joinToString()}"),
        )

        "create" -> listOf(
            command("$CMD create ", "$CMD create <id> <target>"),
            note("Spawns a native NPC at your position, links it, and opens its editor."),
            note("Example: $CMD create lobby Lobby"),
        )

        "link" -> listOf(
            command("$CMD link ", "$CMD link <id> <provider> <reference> <target>"),
            note("Attaches hologram and click handling to an NPC you created with"),
            note("Citizens, FancyNpcs, ZNPCsPlus or MythicMobs. The NPC itself is never changed."),
            note("Example: $CMD link lobby citizens 42 Lobby"),
            command("$CMD unlink ", "$CMD unlink <id>", "Detach again, the NPC stays"),
        )

        "edit" -> listOf(
            command("$CMD edit", "$CMD edit", "Edit the NPC you're looking at"),
            command("$CMD edit ", "$CMD edit <id> target"),
            command("$CMD edit ", "$CMD edit <id> hologram"),
            command("$CMD edit ", "$CMD edit <id> action"),
            command("$CMD edit ", "$CMD edit <id> pushback"),
            command("$CMD edit ", "$CMD edit <id> teleport"),
            command("$CMD edit ", "$CMD edit <id> skin <player>"),
            note("Use $CMD help <topic> for details."),
        )

        "target" -> listOf(
            command("$CMD edit ", "$CMD edit <id> target list"),
            command("$CMD edit ", "$CMD edit <id> target add <target>"),
            command("$CMD edit ", "$CMD edit <id> target remove <target>"),
        )

        "hologram" -> listOf(
            command("$CMD edit ", "$CMD edit <id> hologram enable"),
            command("$CMD edit ", "$CMD edit <id> hologram disable"),
            command("$CMD edit ", "$CMD edit <id> hologram height <value>"),
            command("$CMD edit ", "$CMD edit <id> hologram line add <state> <text>"),
            command("$CMD edit ", "$CMD edit <id> hologram line set <state> <line> <text>"),
            command("$CMD edit ", "$CMD edit <id> hologram line remove <state> <line>"),
        )

        "action" -> listOf(
            note("$CMD edit <id> action <type> <interaction> <state> [value]"),
            note("Types: join-target, open-inventory, play-sound,"),
            note("  send-message, execute-command, teleport-here, title,"),
            note("  subtitle, title-duration, action-bar, send-to-server, transfer-to-server"),
            note("$CMD edit <id> action clear <interaction> <state> [field]"),
            note("Fields: ${ActionFields.NAMES.joinToString()}"),
        )

        "pushback" -> listOf(
            command("$CMD edit ", "$CMD edit <id> pushback enable"),
            command("$CMD edit ", "$CMD edit <id> pushback disable"),
            command("$CMD edit ", "$CMD edit <id> pushback radius <value>"),
            command("$CMD edit ", "$CMD edit <id> pushback strength <value>"),
            command("$CMD edit ", "$CMD edit <id> pushback vertical <value>"),
            command("$CMD edit ", "$CMD edit <id> pushback sound <sound>"),
            command("$CMD edit ", "$CMD edit <id> pushback sound clear"),
        )

        "admin" -> listOf(
            command("$CMD list", "$CMD list", "List NPCs"),
            command("$CMD info", "$CMD info [id]", "View details (or the one you're looking at)"),
            command("$CMD tp ", "$CMD tp <id>", "Teleport to an NPC"),
            command("$CMD duplicate ", "$CMD duplicate <id> <newId>", "Copy an NPC to where you stand"),
            command("$CMD delete", "$CMD delete [id]", "Delete completely"),
            command("$CMD reload", "$CMD reload", "Reload configs"),
            command("$CMD placeholders", "$CMD placeholders", "List placeholders"),
        )

        "inventory" -> listOf(
            command("$CMD inventory create", "$CMD inventory create", "Create wizard"),
            command("$CMD inventory create ", "$CMD inventory create <id> <rows>"),
            command("$CMD inventory edit ", "$CMD inventory edit <id>", "Visual editor"),
            command("$CMD inventory delete ", "$CMD inventory delete <id>"),
            command("$CMD inventory open ", "$CMD inventory open <id> [player]", "Open it, e.g. from a compass item"),
            command("$CMD inventory list", "$CMD inventory list"),
            note("Reference from an NPC via $CMD edit <id> action open-inventory"),
        )

        else -> null
    }

    private fun command(suggestion: String, usage: String, description: String? = null): String {
        val escaped = suggestion.replace("'", "\\'")
        val tip = listOfNotNull(
            description?.let { "${Msg.INFO}${Msg.miniMessage.escapeTags(it)}" },
            "${Msg.INTERACTIVE}Click to type this command",
        ).joinToString("\n").replace("'", "\\'")
        val line = "${Msg.SUBTLE}• ${Msg.INTERACTIVE}${Msg.miniMessage.escapeTags(usage)}"
        val hover = "<hover:show_text:'$tip'>$line</hover>"

        return "<click:suggest_command:'$escaped'>$hover</click>"
    }

    private fun note(text: String): String = "  ${Msg.MUTED}${Msg.miniMessage.escapeTags(text)}"

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
