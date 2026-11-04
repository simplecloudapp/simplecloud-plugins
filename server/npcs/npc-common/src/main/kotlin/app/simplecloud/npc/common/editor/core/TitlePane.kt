package app.simplecloud.npc.common.editor.core

import app.simplecloud.npc.common.editor.PromptFormat
import app.simplecloud.npc.common.editor.PromptTokens
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.npc.NpcFormat
import app.simplecloud.npc.common.editor.ui.Layout
import app.simplecloud.npc.common.editor.ui.Stepper
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.item.NpcItem
import app.simplecloud.npc.core.config.NpcConfig.TitleConfiguration
import app.simplecloud.npc.core.platform.NpcPlayer

object TitlePane {
    const val SIZE = Layout.SIZE

    private val fadeRange = TitleConfiguration.FADE_RANGE.toDoubleRange()
    private val stayRange = TitleConfiguration.STAY_RANGE.toDoubleRange()

    private const val TITLE_SLOT = 21
    private const val SUBTITLE_SLOT = 23
    private const val PACING_SLOT = Layout.HEADER
    private const val FADE_IN_SLOT = 29
    private const val STAY_SLOT = 31
    private const val FADE_OUT_SLOT = 33
    private const val REMOVE_SLOT = Layout.TOOL_RIGHT

    private val DEFAULTS = TitleConfiguration()

    private fun IntRange.toDoubleRange(): ClosedFloatingPointRange<Double> = first.toDouble()..last.toDouble()

    private fun ticksLine(ticks: Double) =
        "<val>${NpcFormat.seconds(ticks.toInt(), 2)} <hnt>· ${ticks.toInt()} ticks"

    private val FADE_IN = ticksStepper("Fade In", fadeRange, 1.0, DEFAULTS.fadeIn, "SUGAR")
    private val STAY = ticksStepper("Stay", stayRange, 5.0, DEFAULTS.stay, "CLOCK")
    private val FADE_OUT = ticksStepper("Fade Out", fadeRange, 1.0, DEFAULTS.fadeOut, "GUNPOWDER")

    private fun ticksStepper(
        label: String,
        range: ClosedFloatingPointRange<Double>,
        step: Double,
        default: Int,
        material: String,
    ) = Stepper(label, range, step, default.toDouble(), 0, material, bigMultiplier = 5, valueLine = ::ticksLine)

    class Port(
        val player: NpcPlayer,
        val title: TitleConfiguration?,
        val textPrompts: TextPrompts,
        val edit: ((TitleConfiguration) -> TitleConfiguration) -> Unit,
        val removeAndBack: () -> Unit,
        val scope: List<String> = emptyList(),
        val removable: Boolean = title != null,
    )

    fun normalized(title: TitleConfiguration): TitleConfiguration? =
        title.takeIf { it.title.isNotBlank() || it.subtitle.isNotBlank() }

    fun defaults(): TitleConfiguration = DEFAULTS

    fun fill(pane: Pane, port: Port) {
        val title = port.title ?: DEFAULTS

        fun ask(label: String, value: String, set: (TitleConfiguration, String) -> TitleConfiguration) {
            port.textPrompts.open(
                port.player,
                title = label,
                instruction = "Type the ${label.lowercase()} in chat.",
                current = value,
                format = PromptFormat.MINI_MESSAGE,
                placeholders = PromptTokens.PLAYER,
                onClear = { port.edit { set(it, "") } }.takeIf { value.isNotBlank() },
            ) { text ->
                port.edit { set(it, text) }
            }
        }

        pane.left(TITLE_SLOT, textField("OAK_HANGING_SIGN", "Title Text", title.title)) {
            ask("Title Text", title.title) { current, text -> current.copy(title = text) }
        }
        pane.left(SUBTITLE_SLOT, textField("PAPER", "Subtitle Text", title.subtitle)) {
            ask("Subtitle Text", title.subtitle) { current, text -> current.copy(subtitle = text) }
        }

        val total = title.fadeIn + title.stay + title.fadeOut
        pane[PACING_SLOT] = Ui.item(
            "PAINTING",
            "<ttl>Title on Screen",
            listOf(
                "<bd>Title: " + if (title.title.isBlank()) "<off>Not set" else "<val>${Ui.quote(title.title)}",
                "<bd>Subtitle: " + if (title.subtitle.isBlank()) "<off>Not set" else "<val>${Ui.quote(title.subtitle)}",
                "",
                NpcFormat.pacing(title),
                "<bd>On screen for <val>${NpcFormat.seconds(total)}",
                "",
            ) + port.scope,
        )

        if (port.title == null) {
            val hint = listOf("<hnt>Set a title or subtitle first.")
            listOf(FADE_IN_SLOT to FADE_IN, STAY_SLOT to STAY, FADE_OUT_SLOT to FADE_OUT).forEach { (slot, stepper) ->
                pane[slot] = stepper.disabled(hint)
            }
        } else {
            pane[FADE_IN_SLOT] = FADE_IN.element(title.fadeIn.toDouble()) { value ->
                port.edit { it.copy(fadeIn = value.toInt()) }
            }
            pane[STAY_SLOT] = STAY.element(title.stay.toDouble()) { value ->
                port.edit { it.copy(stay = value.toInt()) }
            }
            pane[FADE_OUT_SLOT] = FADE_OUT.element(title.fadeOut.toDouble()) { value ->
                port.edit { it.copy(fadeOut = value.toInt()) }
            }
        }

        if (!port.removable) {
            pane[REMOVE_SLOT] = Ui.disabled("Remove Title", listOf("<hnt>There is no title to remove."), "RED_CONCRETE")
            return
        }
        pane.left(
            REMOVE_SLOT,
            Ui.item(
                "RED_CONCRETE",
                "<err>Remove Title",
                listOf("<hnt>Other parts of the action stay.", "<key>Left-click to remove it"),
            ),
        ) { port.removeAndBack() }
    }

    private fun textField(material: String, label: String, value: String): NpcItem = Ui.item(
        material,
        "<ttl>$label",
        listOf(
            if (value.isBlank()) "<off>Not set" else "<val>${Ui.quote(value)}",
            "",
            "<key>Left-click to type it in chat",
        ),
    )
}
