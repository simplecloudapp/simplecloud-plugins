package app.simplecloud.npc.common.editor.core

import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.menu.MenuClick
import app.simplecloud.npc.common.editor.menu.Paginator
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.ui.Layout
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.item.NpcItem
import app.simplecloud.npc.core.platform.NpcPlayer

class PickerOption(val value: String, val item: NpcItem, val available: Boolean = true)

class PickerSpec<S : Any>(
    val title: String,
    val screen: S,
    val options: List<PickerOption>,
    val onPick: (String) -> Unit,
    val typeHint: List<String>? = DEFAULT_TYPE_HINT,
    val typingUnavailable: String = "Pick one from the list above.",
    val onTyped: (String) -> PickerResult? = {
        onPick(it)
        null
    },
    val onRightClick: (PickerOption) -> Unit = {},
    val extras: (Pane) -> Unit = {},
    val filteredNoun: String = "options",
    val gridRows: Int = 3,
) {
    companion object {
        val DEFAULT_TYPE_HINT = listOf(
            "<bd>Not in the list above?",
            "",
            "<key>Left-click to type it in chat",
        )
    }
}

object PickerTemplate {
    const val SIZE = Layout.SIZE

    private const val TYPE_POSITION = 3
    val TYPE_SLOT = Layout.tool(TYPE_POSITION)
    val RESULT_SLOT = Layout.tool(0)

    private const val RESULT_TTL_TICKS = 60L

    fun <S : Any> build(host: EditorHost<S>, player: NpcPlayer, spec: PickerSpec<S>): EditorMenu {
        val session = host.session(player)
        val pages = Paginator(session.pages, pageKey(spec.screen), spec.options, Layout.ROW_SLOTS * spec.gridRows)
        val grid = Layout.list(pages.visible.size, rows = spec.gridRows)
        val pane = Pane(SIZE)

        fun rerender() = host.render(player, spec.screen)

        pages.visible.forEachIndexed { index, option ->
            pane.on(grid[index], option.item) { click ->
                when (click) {
                    MenuClick.RIGHT -> spec.onRightClick(option)
                    MenuClick.LEFT -> if (option.available) spec.onPick(option.value)
                    else -> Unit
                }
            }
        }

        val typeHint = spec.typeHint
        if (typeHint == null) {
            pane[TYPE_SLOT] = Ui.disabled("Type A Name", listOf("<hnt>${spec.typingUnavailable}"))
        } else {
            pane.left(TYPE_SLOT, Ui.item("ANVIL", "<ttl>Type A Name", typeHint)) { promptTyped(host, player, spec) }
        }

        spec.extras(pane)

        session.pickerResult?.let { result ->
            pane.on(RESULT_SLOT, resultItem(result, spec.filteredNoun), MenuClick.DROP) {
                session.pickerResult = null
                rerender()
            }
        }

        pane.back(host, player)
        pane.pager(pages, listOf("<bd>${spec.options.size} options", "<hnt>${pages.perPage} fit per page."), ::rerender)

        return pane.menu(spec.title)
    }

    fun <S : Any> showResult(host: EditorHost<S>, player: NpcPlayer, screen: S, result: PickerResult) {
        val session = host.sessions.peek(player.uniqueId) ?: return
        session.pickerResult = result
        host.render(player, screen)

        host.runLater(RESULT_TTL_TICKS) {
            val current = host.sessions.peek(player.uniqueId) ?: return@runLater
            if (current.pickerResult !== result) return@runLater
            current.pickerResult = null
            if (host.isShowing(player) { it == screen }) host.render(player, screen)
        }
    }

    private fun <S : Any> promptTyped(host: EditorHost<S>, player: NpcPlayer, spec: PickerSpec<S>) {
        host.textPrompts.open(
            player,
            title = "Search",
            instruction = "Type a name in chat; part of one filters the list.",
            current = null,
            validate = { text -> "Type a name.".takeIf { text.isBlank() } },
        ) { text ->
            val result = spec.onTyped(text) ?: return@open
            val session = host.session(player)
            session.pickerResult = result
            if (result is PickerResult.Filtered) session.pages[pageKey(spec.screen)] = 0
            host.render(player, spec.screen)
        }
    }

    private fun resultItem(result: PickerResult, filteredNoun: String): NpcItem = when (result) {
        is PickerResult.Added -> Ui.item("LIME_DYE", "<on>Added: <val>${result.label}", listOf("<hnt>${result.detail}"))

        is PickerResult.Rejected -> Ui.item(
            "RED_DYE",
            "<err>${result.headline}: <val>${Ui.quote(result.typed)}",
            listOf("<hnt>${result.reason}", "<key>Press Q to dismiss it"),
        )

        is PickerResult.Filtered -> Ui.item(
            "ORANGE_DYE",
            "<warn>Filtered: <val>${Ui.quote(result.typed)}",
            listOf(
                "<bd><val>${result.count} <bd>matching $filteredNoun",
                "",
                "<hnt>Pick one above, or type more.",
                "",
                "<key>Press Q to clear the filter",
            ),
        )

        is PickerResult.Ambiguous -> Ui.item(
            "ORANGE_DYE",
            "<warn>Ambiguous: <val>${Ui.quote(result.typed)}",
            listOf(
                "<bd>Matches " + result.matches.joinToString("<bd>, ") { "<val>$it" },
                "",
                "<hnt>Pick one above, or type the full name.",
                "",
                "<key>Press Q to dismiss it",
            ),
        )
    }
}
