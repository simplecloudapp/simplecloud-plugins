package app.simplecloud.npc.common.editor

import app.simplecloud.npc.common.text.Msg
import app.simplecloud.npc.core.text.NpcPalette
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import java.time.Duration

object PromptCard {

    class Controls(
        val cancel: () -> Unit,
        val clear: (() -> Unit)?,
        val apply: (String) -> Unit,
        val extra: ((String) -> Unit)?,
    )

    private const val INDENT = "   "
    private const val FORMAT_DOCS = "https://docs.advntr.dev/minimessage/format.html"

    private val BRAND get() = NpcPalette.color("brand")
    private val INTERACTIVE get() = NpcPalette.color("interactive")
    private val ACCENT get() = NpcPalette.color("highlight")
    private val MUTED get() = NpcPalette.color("muted")
    private val SUBTLE get() = NpcPalette.color("separator")
    private val SUCCESS get() = NpcPalette.color("success")
    private val WARNING get() = NpcPalette.color("warning")
    private val ERROR get() = NpcPalette.color("error")

    private val CALLBACK_OPTIONS = ClickCallback.Options.builder()
        .uses(1)
        .lifetime(Duration.ofMinutes(15))
        .build()

    private val FORMAT_EXAMPLES = listOf(
        "<brand>A palette color",
        "<red>Red text",
        "<#3B95FF>Any hex color",
        "<gradient:#3B95FF:#6AD13C>A gradient</gradient>",
        "<b>Bold</b> <i>italic</i> <u>underlined</u>",
        "<rainbow>Rainbow</rainbow>",
    )

    fun open(prompt: Prompt, controls: Controls): Component {
        val current = prompt.current
        val clear = controls.clear

        return card(
            listOfNotNull(
                header(prompt, null),
                instruction(prompt),
                current(prompt),
                placeholders(prompt),
                Component.empty(),
                buttons(
                    listOfNotNull(
                        if (current == null) null else suggestButton("Edit current", current),
                        if (clear == null) null else button("Clear", WARNING, text("Removes the value.", MUTED), callback(clear)),
                        if (prompt.format == PromptFormat.MINI_MESSAGE) formatButton() else null,
                        cancelButton(prompt, controls),
                    ),
                ),
            ),
        )
    }

    fun preview(prompt: Prompt, typed: String, controls: Controls): Component {
        val extra = prompt.extra
        val run = controls.extra
        val extraButton = if (extra == null || run == null) {
            null
        } else {
            button(extra.label, INTERACTIVE, text(extra.hint, MUTED), callback { run(typed) })
        }

        return card(
            listOf(
                header(prompt, "Preview"),
                Msg.miniMessage.deserialize(typed),
                Component.empty(),
                buttons(
                    listOfNotNull(
                        button("Apply", SUCCESS, text("Saves it exactly like this.", MUTED), callback { controls.apply(typed) }),
                        extraButton,
                        suggestButton("Edit", typed),
                        cancelButton(prompt, controls),
                    ),
                ),
            ),
        )
    }

    fun rejected(prompt: Prompt, reason: Component, controls: Controls): Component = Component.text()
        .append(text("⚡ ", BRAND))
        .append(reason.colorIfAbsent(ERROR))
        .append(text("  Try again, or ", MUTED))
        .append(cancelButton(prompt, controls))
        .build()

    fun failed(): Component = Component.text()
        .append(text("⚡ ", BRAND))
        .append(text("Something went wrong, so the input was cancelled.", ERROR))
        .build()

    fun hud(prompt: Prompt, staged: Boolean): Component = if (staged) {
        Component.text()
            .append(text("Preview ready", SUCCESS))
            .append(text("  ·  click ", SUBTLE))
            .append(text("Apply", SUCCESS))
            .append(text(" or type again", SUBTLE))
            .build()
    } else {
        Component.text()
            .append(text("⚡ ", BRAND))
            .append(text(prompt.title, ACCENT))
            .append(text("  ·  type in chat  ·  ", SUBTLE))
            .append(text(prompt.cancelKeyword, MUTED))
            .append(text(" to stop", SUBTLE))
            .build()
    }

    private fun card(lines: List<Component>): Component {
        val body = lines.mapIndexed { index, line -> Component.text(if (index == 0) " " else INDENT).append(line) }

        return Component.join(JoinConfiguration.newlines(), listOf(Component.empty()) + body + Component.empty())
    }

    private fun header(prompt: Prompt, suffix: String?): Component {
        val builder = Component.text()
            .append(text("⚡ ", BRAND))
            .append(text(prompt.title, ACCENT))
        suffix?.let { builder.append(text("  ·  ", SUBTLE)).append(text(it, ACCENT)) }
        prompt.context?.let { builder.append(text("  ·  ", SUBTLE)).append(text(it, SUBTLE)) }

        return builder.build()
    }

    private fun instruction(prompt: Prompt): Component {
        val highlighted = Msg.highlight(
            Msg.miniMessage.escapeTags(prompt.instruction),
            prompt.instructionArgs.toTypedArray(),
        )

        return Msg.miniMessage.deserialize(Msg.MUTED + highlighted)
    }

    private fun current(prompt: Prompt): Component? {
        val value = prompt.current?.takeIf { it.isNotBlank() } ?: return null
        val shown = if (prompt.format == PromptFormat.MINI_MESSAGE) {
            runCatching { Msg.miniMessage.deserialize(value) }.getOrElse { text(value, ACCENT) }
        } else {
            text(value, ACCENT)
        }
        val hover = Component.join(
            JoinConfiguration.newlines(),
            text(value, MUTED),
            Component.empty(),
            text("Click to put it into your chat bar", INTERACTIVE),
        )

        return Component.text()
            .append(text("Current  ", SUBTLE))
            .append(shown.hoverEvent(HoverEvent.showText(hover)).clickEvent(ClickEvent.suggestCommand(value)))
            .build()
    }

    private fun placeholders(prompt: Prompt): Component? {
        if (prompt.placeholders.isEmpty()) return null
        val chips = prompt.placeholders.map { placeholder ->
            val hover = Component.join(
                JoinConfiguration.newlines(),
                text(placeholder.token, ACCENT),
                text(placeholder.description, MUTED),
                Component.empty(),
                text("Click to copy, then paste it into your text", INTERACTIVE),
            )
            Component.text()
                .append(text(placeholder.token, INTERACTIVE))
                .hoverEvent(HoverEvent.showText(hover))
                .clickEvent(ClickEvent.copyToClipboard(placeholder.token))
                .build()
        }

        return Component.text()
            .append(text("Insert   ", SUBTLE))
            .append(Component.join(JoinConfiguration.separator(text("  ", SUBTLE)), chips))
            .build()
    }

    private fun buttons(buttons: List<Component>): Component =
        Component.join(JoinConfiguration.separator(Component.text("  ")), buttons)

    private fun cancelButton(prompt: Prompt, controls: Controls): Component = button(
        "Cancel",
        ERROR,
        text("Back to the menu. Typing '${prompt.cancelKeyword}' does the same.", MUTED),
        callback(controls.cancel),
    )

    private fun suggestButton(label: String, value: String): Component = button(
        label,
        INTERACTIVE,
        text("Puts the text into your chat bar to change it.", MUTED),
        ClickEvent.suggestCommand(value),
    )

    private fun formatButton(): Component {
        val examples = FORMAT_EXAMPLES.map { example ->
            Component.text()
                .append(text(example, SUBTLE))
                .append(text("  →  ", SUBTLE))
                .append(Msg.miniMessage.deserialize(example))
                .build()
        }
        val hover = Component.join(
            JoinConfiguration.newlines(),
            listOf(text("MiniMessage formatting", ACCENT), Component.empty()) + examples +
                listOf(Component.empty(), text("Click to open the full reference", INTERACTIVE)),
        )

        return button("Format help", INTERACTIVE, hover, ClickEvent.openUrl(FORMAT_DOCS))
    }

    private fun button(label: String, color: TextColor, hover: Component, click: ClickEvent): Component =
        Component.text()
            .append(text(label, color))
            .hoverEvent(HoverEvent.showText(hover))
            .clickEvent(click)
            .build()

    private fun callback(action: () -> Unit): ClickEvent = ClickEvent.callback({ action() }, CALLBACK_OPTIONS)

    private fun text(content: String, color: TextColor): Component = Component.text(content, color)
}
