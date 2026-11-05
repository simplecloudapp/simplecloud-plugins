package app.simplecloud.npc.common.text

import app.simplecloud.npc.core.platform.NpcCommandSender
import app.simplecloud.npc.core.text.NpcPalette
import net.kyori.adventure.text.minimessage.MiniMessage

object Msg {
    const val PREFIX = "<brand><b>⚡</b></brand>"

    const val ERROR = "<error>"
    const val WARNING = "<warning>"
    const val SUCCESS = "<success>"
    const val INFO = "<info>"
    const val KEY = "<muted>"
    const val INTERACTIVE = "<interactive>"
    const val HIGHLIGHT = "<highlight>"

    const val MUTED = "<muted>"
    const val SUBTLE = "<separator>"

    private val PLACEHOLDER = Regex.fromLiteral("{}")

    val miniMessage: MiniMessage = NpcPalette.miniMessage

    fun error(template: String, vararg args: Any?) = compose(ERROR, template, args)
    fun warning(template: String, vararg args: Any?) = compose(WARNING, template, args)
    fun success(template: String, vararg args: Any?) = compose(SUCCESS, template, args)
    fun info(template: String, vararg args: Any?) = compose(INFO, template, args)

    fun errorPrefix() = prefix(ERROR)
    fun successPrefix() = prefix(SUCCESS)
    fun infoPrefix() = prefix(INFO)

    private fun prefix(color: String) = "$PREFIX $color"

    private fun compose(color: String, template: String, args: Array<out Any?>): String =
        prefix(color) + highlight(template, args)

    /** A bulleted list. */
    fun tree(lines: List<String>, indent: String = ""): List<String> = lines.map { line -> "$indent$SUBTLE• $line" }

    fun highlight(template: String, args: Array<out Any?>): String {
        if (args.isEmpty()) return template
        var argIndex = 0

        return PLACEHOLDER.replace(template) { match ->
            if (argIndex < args.size) {
                miniMessage.escapeTags(args[argIndex++].toString())
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
