package app.simplecloud.npc.common.editor.core

import app.simplecloud.npc.common.editor.menu.Paginator
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.core.platform.NpcPlayer

fun Pane.back(host: EditorHost<*>, player: NpcPlayer) =
    left(navRow.first, Ui.back()) { host.back(player) }

fun Pane.toggle(
    slot: Int,
    label: String,
    enabled: Boolean,
    extraLore: List<String> = emptyList(),
    material: String? = null,
    onClick: () -> Unit,
) = left(slot, Ui.toggle(label, enabled, extraLore, material), onClick)

fun pageKey(screen: Any): String = screen.toString()

/** Previous in the first and Next in the last slot of the bottom row, only when that page exists. */
fun Pane.pager(pages: Paginator<*>, lore: List<String>, rerender: () -> Unit) {
    if (pages.pageCount <= 1) return
    val previous = Ui.previousPage(pages.page)
    if (previous != null) {
        left(navRow.first, previous) {
            pages.turn(-1)
            rerender()
        }
    }
    this[navRow.first + 4] = Ui.pageIndicator(pages.page, pages.pageCount, lore)
    val next = Ui.nextPage(pages.page, pages.pageCount)
    if (next != null) {
        left(navRow.last, next) {
            pages.turn(1)
            rerender()
        }
    }
}
