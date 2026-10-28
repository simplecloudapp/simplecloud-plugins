package app.simplecloud.npc.common.text

import app.simplecloud.npc.core.platform.NpcCommandSender
import net.kyori.adventure.text.minimessage.MiniMessage

object Msg {
    const val PREFIX = "<#38bdf8><b>⚡</b>"

    const val ERROR = "<#dc2626>"
    const val WARNING = "<#f59e0b>"
    const val SUCCESS = "<#a3e635>"
    const val INFO = "<#ffffff>"
    const val KEY = "<#38bdf8>"

    const val ACCENT = "<#fbbf24>"
    const val MUTED = "<#a3a3a3>"
    const val SUBTLE = "<#a8a8a8>"

    const val SEPARATOR = "<#a8a8a8>════════════════════════"

    private val PLACEHOLDER = Regex.fromLiteral("{}")

    val miniMessage: MiniMessage = MiniMessage.miniMessage()

    fun error(template: String, vararg args: Any?) = compose(ERROR, template, args)
    fun warning(template: String, vararg args: Any?) = compose(WARNING, template, args)
    fun success(template: String, vararg args: Any?) = compose(SUCCESS, template, args)
    fun info(template: String, vararg args: Any?) = compose(INFO, template, args)

    fun errorPrefix() = prefix(ERROR)
    fun successPrefix() = prefix(SUCCESS)
    fun infoPrefix() = prefix(INFO)

    private fun prefix(color: String) = "$PREFIX $color"

    private fun compose(color: String, template: String, args: Array<out Any?>): String =
        prefix(color) + highlight(template, args, color)

    /** Sign-style tree: `├─` for every line but the last, which gets `└─`. */
    fun tree(lines: List<String>, indent: String = ""): List<String> = lines.mapIndexed { index, line ->
        val branch = if (index == lines.lastIndex) "└─" else "├─"
        "$indent$SUBTLE$branch $line"
    }

    fun highlight(template: String, args: Array<out Any?>, baseColor: String): String {
        if (args.isEmpty()) return template
        var argIndex = 0

        return PLACEHOLDER.replace(template) { match ->
            if (argIndex < args.size) {
                "$ACCENT${miniMessage.escapeTags(args[argIndex++].toString())}$baseColor"
            } else {
                match.value
            }
        }
    }
}

fun String.substitute(substitutions: Map<String, String>): String =
    substitutions.entries.fold(this) { acc, (key, replacement) -> acc.replace(key, replacement) }

fun NpcCommandSender.sendError(template: String, vararg args: Any?) =
    sendMessage(Msg.miniMessage.deserialize(Msg.error(template, *args)))

fun NpcCommandSender.sendWarning(template: String, vararg args: Any?) =
    sendMessage(Msg.miniMessage.deserialize(Msg.warning(template, *args)))

fun NpcCommandSender.sendSuccess(template: String, vararg args: Any?) =
    sendMessage(Msg.miniMessage.deserialize(Msg.success(template, *args)))

fun NpcCommandSender.sendInfo(template: String, vararg args: Any?) =
    sendMessage(Msg.miniMessage.deserialize(Msg.info(template, *args)))
