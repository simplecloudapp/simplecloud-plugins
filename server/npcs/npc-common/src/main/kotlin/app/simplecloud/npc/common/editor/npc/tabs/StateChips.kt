package app.simplecloud.npc.common.editor.npc.tabs

import app.simplecloud.npc.common.editor.menu.MenuClick
import app.simplecloud.npc.common.editor.menu.Paginator
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.npc.NpcEditorContext
import app.simplecloud.npc.common.editor.npc.NpcEditorScreen
import app.simplecloud.npc.common.editor.npc.NpcFormat
import app.simplecloud.npc.common.editor.ui.Layout
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.item.NpcItem
import app.simplecloud.npc.core.platform.NpcPlayer

class StateChips(
    val states: List<String>,
    val selected: String,
    val live: String?,
    val detail: (String) -> String?,
    val select: (String) -> Unit,
    val remove: (String) -> Unit,
    val add: StateChipRow.Add? = null,
)

/** The chips of all join states and the add button, as one centered run in the first content row. */
object StateChipRow {
    private const val VISIBLE_CHIPS = 4
    private const val SELECTED_MATERIAL = "LIGHT_BLUE_CONCRETE"
    private const val OTHER_MATERIAL = "WHITE_CONCRETE"

    class Add(val item: NpcItem, val onClick: () -> Unit)

    fun place(
        pane: Pane,
        context: NpcEditorContext,
        player: NpcPlayer,
        screen: NpcEditorScreen,
        key: String,
        chips: StateChips,
    ) {
        val add = chips.add
        val showChips = chips.states.isNotEmpty()
        val pages = Paginator(context.session(player).pages, key, chips.states, VISIBLE_CHIPS)
        val previous = if (showChips) Ui.previousPage(pages.page) else null
        val next = if (showChips) Ui.nextPage(pages.page, pages.pageCount) else null
        val visible = if (showChips) pages.visible else emptyList()
        val count = listOfNotNull(previous, next, add).size + visible.size
        val slots = Layout.row(1, count).iterator()

        if (previous != null) {
            pane.left(slots.next(), previous) {
                pages.turn(-1)
                context.render(player, screen)
            }
        }
        visible.forEach { state -> chip(pane, slots.next(), chips, state) }
        if (next != null) {
            pane.left(slots.next(), next) {
                pages.turn(1)
                context.render(player, screen)
            }
        }
        if (add != null) pane.left(slots.next(), add.item, add.onClick)
    }

    private fun chip(pane: Pane, slot: Int, chips: StateChips, state: String) {
        val selected = state == chips.selected
        val removable = !NpcFormat.isDefaultJoinState(state)
        val lore = listOfNotNull(
            chips.detail(state),
            "<on>Live now".takeIf { state == chips.live },
            "<hnt>Shown below".takeIf { selected },
            if (NpcFormat.isDefaultJoinState(state)) "<bd>Used when no other join state fits." else null,
            if (chips.states.size == 1) "<hnt>A join state is what the target is doing." else null,
            "",
            "<key>Left-click to show it".takeIf { !selected },
            "<key>Right-click to remove it".takeIf { removable },
        )
        val label = NpcFormat.joinState(state) + if (removable) "" else " · Default"
        val name = if (selected) "<val>$label" else "<ttl>$label"
        val material = if (selected) SELECTED_MATERIAL else OTHER_MATERIAL
        pane.on(slot, Ui.item(material, name, lore, glowing = selected)) { click ->
            when (click) {
                MenuClick.LEFT -> if (!selected) chips.select(state)
                MenuClick.RIGHT -> if (removable) chips.remove(state)
                else -> Unit
            }
        }
    }
}
