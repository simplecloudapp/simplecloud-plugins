package app.simplecloud.npc.common.editor.npc.tabs

import app.simplecloud.npc.common.editor.menu.MenuClick
import app.simplecloud.npc.common.editor.menu.Paginator
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.npc.NpcEditorContext
import app.simplecloud.npc.common.editor.npc.NpcEditorScreen
import app.simplecloud.npc.common.editor.npc.NpcFormat
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.core.platform.NpcPlayer

class StateChips(
    val states: List<String>,
    val selected: String,
    val live: String?,
    val detail: (String) -> String?,
    val select: (String) -> Unit,
    val remove: (String) -> Unit,
)

object StateChipRow {
    private const val PREVIOUS_SLOT = 20
    private val CHIP_SLOTS = 21..24
    private const val NEXT_SLOT = 25
    private const val SELECTED_MATERIAL = "LIGHT_BLUE_CONCRETE"
    private const val OTHER_MATERIAL = "WHITE_CONCRETE"

    fun place(
        pane: Pane,
        context: NpcEditorContext,
        player: NpcPlayer,
        screen: NpcEditorScreen,
        key: String,
        chips: StateChips,
    ) {
        if (chips.states.size <= 1) return
        val pages = Paginator(context.session(player).pages, key, chips.states, CHIP_SLOTS.count())

        pages.visible.forEachIndexed { index, state ->
            val selected = state == chips.selected
            val removable = !NpcFormat.isDefaultJoinState(state)
            val label = NpcFormat.joinState(state)
            val hints = listOfNotNull(
                "<key>Left-click to show it".takeIf { !selected },
                "<key>Right <err>Remove".takeIf { removable },
            ).joinToString(" <hnt>· ").ifEmpty { null }
            val lore = listOfNotNull(
                chips.detail(state),
                "<on>Live now".takeIf { state == chips.live },
                "<hnt>Shown below".takeIf { selected },
                hints,
            )

            val live = if (state == chips.live) "<on>● " else ""
            val name = if (selected) "$live<val>$label" else "$live<bd>$label"
            val material = if (selected) SELECTED_MATERIAL else OTHER_MATERIAL
            pane.on(CHIP_SLOTS.first + index, Ui.item(material, name, lore, glowing = selected)) { click ->
                when (click) {
                    MenuClick.LEFT -> if (!selected) chips.select(state)
                    MenuClick.RIGHT -> if (removable) chips.remove(state)
                    else -> Unit
                }
            }
        }

        val previous = Ui.previousPage(pages.page)
        if (previous != null) {
            pane.left(PREVIOUS_SLOT, previous) {
                pages.turn(-1)
                context.render(player, screen)
            }
        }
        val next = Ui.nextPage(pages.page, pages.pageCount)
        if (next != null) {
            pane.left(NEXT_SLOT, next) {
                pages.turn(1)
                context.render(player, screen)
            }
        }
    }
}
